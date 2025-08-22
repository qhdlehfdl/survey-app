import React, { useState, ChangeEvent, FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./RegisterForm.module.css";

interface FormData {
  userId: string;
  password: string;
  nickname: string;
  email: string;
}

interface FormErrors {
  userId?: string;
  password?: string;
  nickname?: string;
  email?: string;
  general?: string;
}

interface ApiResponse {
  code: string;
  message: string;
}

const RegisterForm: React.FC = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<FormData>({
    userId: "",
    password: "",
    nickname: "",
    email: "",
  });
  const [errors, setErrors] = useState<FormErrors>({});

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined, general: undefined }));
  };

  const validateClient = (): boolean => {
    const newErrors: FormErrors = {};
    if (formData.userId.length < 3 || formData.userId.length > 20) {
      newErrors.userId = "아이디는 3자 이상 20자 이하로 입력해야 합니다.";
    }
    if (formData.password.length < 3 || formData.password.length > 20) {
      newErrors.password = "비밀번호는 3자 이상 20자 이하로 입력해야 합니다.";
    }
    if (formData.nickname.length < 3 || formData.nickname.length > 20) {
      newErrors.nickname = "닉네임은 3자 이상 20자 이하로 입력해야 합니다.";
    }
    if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = "유효한 이메일을 입력하세요.";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!validateClient()) return;

    try {
      const resp = await fetch("/api/auth/sign-up", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });
      const data: ApiResponse = await resp.json();

      if (resp.ok && data.code === "SU") {
        alert("회원가입이 완료되었습니다!");
        navigate("/sign-in");
        return;
      }

      const fieldErrors: FormErrors = {};
      switch (data.code) {
        case "DUPLICATE_ID":
          fieldErrors.userId = data.message;
          break;
        case "DUPLICATE_EMAIL":
          fieldErrors.email = data.message;
          break;
        case "DUPLICATE_NICKNAME":
          fieldErrors.nickname = data.message;
          break;
        default:
          fieldErrors.general =
            data.message || "알 수 없는 오류가 발생했습니다.";
      }
      setErrors(fieldErrors);
    } catch (err) {
      console.error(err);
      setErrors({ general: "서버와 통신 중 오류가 발생했습니다." });
    }
  };

  return (
    <div className={styles.registerContainer}>
      <h2 className={styles.registerTitle}>회원가입</h2>
      {errors.general && <p className={styles.errorText}>{errors.general}</p>}
      <form onSubmit={handleSubmit}>
        {/* 아이디 */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>아이디</label>
          <input
            type="text"
            name="userId"
            className={styles.formInput}
            placeholder="아이디를 입력하세요"
            value={formData.userId}
            onChange={handleChange}
            required
          />
          {errors.userId && (
            <p className={styles.errorText}>{errors.userId}</p>
          )}
        </div>

        {/* 비밀번호 */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>비밀번호</label>
          <input
            type="password"
            name="password"
            className={styles.formInput}
            placeholder="비밀번호를 입력하세요"
            value={formData.password}
            onChange={handleChange}
            required
          />
          {errors.password && (
            <p className={styles.errorText}>{errors.password}</p>
          )}
        </div>

        {/* 닉네임 */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>닉네임</label>
          <input
            type="text"
            name="nickname"
            className={styles.formInput}
            placeholder="닉네임을 입력하세요"
            value={formData.nickname}
            onChange={handleChange}
            required
          />
          {errors.nickname && (
            <p className={styles.errorText}>{errors.nickname}</p>
          )}
        </div>

        {/* 이메일 */}
        <div className={styles.formGroup}>
          <label className={styles.formLabel}>이메일</label>
          <input
            type="email"
            name="email"
            className={styles.formInput}
            placeholder="이메일을 입력하세요"
            value={formData.email}
            onChange={handleChange}
            required
          />
          {errors.email && (
            <p className={styles.errorText}>{errors.email}</p>
          )}
        </div>

        <button type="submit" className={styles.registerButton}>
          회원가입
        </button>
      </form>
    </div>
  );
};

export default RegisterForm;
