package edu.cit.paradela.checkmateacademy.features.lesson;

import edu.cit.paradela.checkmateacademy.features.coach.CoachProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CoachProfileService coachProfileService;

    @InjectMocks
    private LessonService lessonService;

    private Lesson testLesson;

    @BeforeEach
    void setUp() {
        testLesson = new Lesson();
        testLesson.setId(UUID.randomUUID());
        testLesson.setCoachId(UUID.randomUUID());
        testLesson.setStartTime(LocalDateTime.now().plusDays(1));
        testLesson.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
    }

    @Test
    void createBooking_Success() {
        when(coachProfileService.isCoachAvailable(any(), any(), any())).thenReturn(true);
        when(lessonRepository.existsOverlappingLesson(any(), any(), any())).thenReturn(false);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

        Lesson savedLesson = lessonService.createBooking(testLesson);

        assertNotNull(savedLesson);
        assertEquals("PENDING", savedLesson.getStatus());
        verify(lessonRepository, times(1)).save(testLesson);
    }

    @Test
    void createBooking_Fail_CoachUnavailable() {
        when(coachProfileService.isCoachAvailable(any(), any(), any())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            lessonService.createBooking(testLesson);
        });

        assertEquals("COACH_UNAVAILABLE", exception.getMessage());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void createBooking_Fail_TimeConflict() {
        when(coachProfileService.isCoachAvailable(any(), any(), any())).thenReturn(true);
        when(lessonRepository.existsOverlappingLesson(any(), any(), any())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            lessonService.createBooking(testLesson);
        });

        assertEquals("TIME_CONFLICT", exception.getMessage());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    void updateBoardState_Success() {
        when(lessonRepository.findById(testLesson.getId())).thenReturn(Optional.of(testLesson));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(testLesson);

        Lesson updatedLesson = lessonService.updateBoardState(testLesson.getId(), "new_fen", "new_pgn");

        assertEquals("new_fen", updatedLesson.getBoardState());
        assertEquals("new_pgn", updatedLesson.getPgnHistory());
    }
}