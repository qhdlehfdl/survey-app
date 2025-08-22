import React, { useEffect, useState } from "react";
import { Routes, Route, BrowserRouter, Navigate } from "react-router-dom";
import logo from "./logo.svg";
import "./App.css";

import LoginForm from "./components/LoginForm/LoginForm";
import RegisterForm from "./components/RegisterForm/RegisterForm";
import SurveyForm from "./components/SurveyForm/SurveyForm";
import MainPage from "./components/MainPage/MainPage";
import SurveyDetail from "./components/SurveyDetail/SurveyDetail";
import ProfilePage from "./components/ProfilePage/ProfilePage";
import SurveyResultPage from "./components/SurveyResultPage/SurveyResultPage";
import SurveyEditPage from "./components/SurveyEditPage/SurveyEditPage"

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/main" replace />} />
      <Route path="/sign-in" element={<LoginForm />} />
      <Route path="/sign-up" element={<RegisterForm />} />
      <Route path="/form" element={<SurveyForm />} />
      <Route path="/main" element={<MainPage />} />
      <Route path="/survey/:id" element={<SurveyDetail />} />
      <Route path="/profile" element={<ProfilePage />} />
      <Route path="/survey/:id/result" element={<SurveyResultPage />} />
      <Route path="/survey/:id/edit" element={<SurveyEditPage />} />
    </Routes>
  );
}

export default App;
