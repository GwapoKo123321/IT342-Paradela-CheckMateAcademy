import React from 'react';
import { Link } from 'react-router-dom';

const Header = () => {
  return (
    <header className="bg-chess-brown p-4 flex justify-between items-center text-white px-10 rounded-b-2xl shadow-lg">
      <h1 className="text-2xl font-serif">CheckMateAcademy</h1>
      <div className="space-x-4">
        <Link to="/login" className="bg-chess-cream text-black px-8 py-2 rounded-full font-bold hover:bg-white transition">Login</Link>
        <Link to="/register" className="bg-chess-cream text-black px-8 py-2 rounded-full font-bold hover:bg-white transition">Register</Link>
      </div>
    </header>
  );
};

export default Header;