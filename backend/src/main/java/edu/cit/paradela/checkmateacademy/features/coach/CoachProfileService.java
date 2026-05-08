package edu.cit.paradela.checkmateacademy.features.coach;

import edu.cit.paradela.checkmateacademy.features.auth.User;
import edu.cit.paradela.checkmateacademy.features.lesson.Lesson;
import edu.cit.paradela.checkmateacademy.features.lesson.LessonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CoachProfileService {

    @Autowired
    private CoachProfileRepository coachProfileRepository;

    @Autowired
    private CoachAvailabilityRepository coachAvailabilityRepository;

    @Autowired
    private LessonRepository lessonRepository;

    private static final int LESSON_LENGTH_MINUTES = 60;
    private static final int SLOT_STEP_MINUTES = 30;

    public CoachProfileResponse buildResponse(User coach) {
        CoachProfile profile = coachProfileRepository.findByCoachId(coach.getId()).orElse(null);
        List<CoachAvailability> availability = coachAvailabilityRepository.findByCoachIdOrderByDayOfWeekAscStartTimeAsc(coach.getId());
        return new CoachProfileResponse(coach, profile, availability);
    }

    public List<CoachProfileResponse> buildResponses(List<User> coaches) {
        return coaches.stream().map(this::buildResponse).toList();
    }

    public List<CoachProfileResponse> filterCoaches(List<User> coaches, LocalDateTime startTime, LocalDateTime endTime, String style) {
        return coaches.stream()
                .map(this::buildResponse)
                .filter(coach -> matchesStyle(coach, style))
                .filter(coach -> startTime == null || endTime == null || isCoachAvailable(coach.getId(), startTime, endTime))
                .toList();
    }

    public List<CoachAvailableSlotResponse> findAvailableLessonSlots(List<User> coaches, LocalDate date, String style) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = date.getDayOfWeek().getValue();

        return coaches.stream()
                .map(this::buildResponse)
                .filter(coach -> matchesStyle(coach, style))
                .flatMap(coach -> {
                    List<Lesson> bookedLessons = lessonRepository.findOverlappingLessons(coach.getId(), dayStart, dayEnd);
                    return coach.getAvailability().stream()
                            .filter(slot -> dayOfWeek == slot.getDayOfWeek())
                            .flatMap(slot -> buildBookableSlots(coach, slot, date, bookedLessons, now).stream());
                })
                .distinct()
                .sorted((first, second) -> first.getStartTime().compareTo(second.getStartTime()))
                .toList();
    }

    @Transactional
    public CoachProfileResponse saveProfile(User coach, CoachProfileRequest request) {
        CoachProfile profile = coachProfileRepository.findByCoachId(coach.getId()).orElseGet(CoachProfile::new);
        profile.setCoachId(coach.getId());
        profile.setSpecialties(request.getSpecialties());
        profile.setBio(request.getBio());
        coachProfileRepository.save(profile);

        coachAvailabilityRepository.deleteByCoachId(coach.getId());
        if (request.getAvailability() != null) {
            for (CoachAvailability slot : request.getAvailability()) {
                if (slot.getDayOfWeek() == null || slot.getStartTime() == null || slot.getEndTime() == null) continue;
                if (!slot.getEndTime().isAfter(slot.getStartTime())) continue;

                CoachAvailability savedSlot = new CoachAvailability();
                savedSlot.setCoachId(coach.getId());
                savedSlot.setDayOfWeek(slot.getDayOfWeek());
                savedSlot.setStartTime(slot.getStartTime());
                savedSlot.setEndTime(slot.getEndTime());
                coachAvailabilityRepository.save(savedSlot);
            }
        }

        return buildResponse(coach);
    }

    public boolean isCoachAvailable(UUID coachId, LocalDateTime startTime, LocalDateTime endTime) {
        int dayOfWeek = startTime.getDayOfWeek().getValue();
        return coachAvailabilityRepository.findByCoachIdAndDayOfWeek(coachId, dayOfWeek).stream()
                .anyMatch(slot ->
                        !startTime.toLocalTime().isBefore(slot.getStartTime())
                                && !endTime.toLocalTime().isAfter(slot.getEndTime())
                );
    }

    private List<CoachAvailableSlotResponse> buildBookableSlots(
            CoachProfileResponse coach,
            CoachAvailability availability,
            LocalDate date,
            List<Lesson> bookedLessons,
            LocalDateTime now
    ) {
        LocalDateTime cursor = LocalDateTime.of(date, availability.getStartTime());
        LocalDateTime availabilityEnd = LocalDateTime.of(date, availability.getEndTime());
        List<CoachAvailableSlotResponse> slots = new java.util.ArrayList<>();

        while (!cursor.plusMinutes(LESSON_LENGTH_MINUTES).isAfter(availabilityEnd)) {
            LocalDateTime slotStart = cursor;
            LocalDateTime slotEnd = cursor.plusMinutes(LESSON_LENGTH_MINUTES);

            if (slotStart.isAfter(now) && bookedLessons.stream().noneMatch(lesson -> overlaps(lesson, slotStart, slotEnd))) {
                slots.add(new CoachAvailableSlotResponse(coach, slotStart, slotEnd));
            }

            cursor = cursor.plusMinutes(SLOT_STEP_MINUTES);
        }

        return slots;
    }

    private boolean overlaps(Lesson lesson, LocalDateTime startTime, LocalDateTime endTime) {
        return lesson.getStartTime().isBefore(endTime) && lesson.getEndTime().isAfter(startTime);
    }

    private boolean matchesStyle(CoachProfileResponse coach, String style) {
        if (style == null || style.isBlank()) return true;
        String specialties = coach.getSpecialties();
        return specialties != null && specialties.toLowerCase().contains(style.toLowerCase());
    }
}
