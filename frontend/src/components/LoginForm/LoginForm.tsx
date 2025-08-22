// src/components/LoginForm/LoginForm.tsx
import React, { useState, ChangeEvent, FormEvent } from "react";
import styles from "./LoginForm.module.css";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { refreshAccessToken } from "../../api/refreshToken";

interface FormData {
  userId: string;
  password: string;
}

interface FormErrors {
  userId?: string;
  password?: string;
  general?: string;
}

interface SignInResponseDto {
  code: string; // e.g. "SU" | "FAIL"
  message: string;
  token?: string; // access token (성공 시)
  expirationTime?: number;
}

const LoginForm: React.FC = () => {
  const navigate = useNavigate();
  const { setAccessToken } = useAuth();

  const [formData, setFormData] = useState<FormData>({
    userId: "",
    password: "",
  });
  const [errors, setErrors] = useState<FormErrors>({});

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined, general: undefined }));
  };

  const validateClient = (): boolean => {
    const newErrors: FormErrors = {};
    if (!formData.userId.trim()) newErrors.userId = "아이디를 입력하세요.";
    if (!formData.password.trim())
      newErrors.password = "비밀번호를 입력하세요.";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!validateClient()) return;

    try {
      const resp = await fetch("/api/auth/sign-in", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      const data: SignInResponseDto = await resp.json();

      // 서버가 access token을 응답으로 주는 경우 (권장)
      if (
        resp.ok &&
        (data.code === "SU" || data.code === "SUCCESS") &&
        data.token
      ) {
        setAccessToken(data.token); // AuthContext에 알려줘서 UI가 즉시 갱신됨
        navigate("/main");
        return;
      }

      // 서버가 access token을 주지 않고 httpOnly refresh 쿠키만 설정한 경우
      // (이때는 refresh 엔드포인트로 access token을 받아와서 setAccessToken)
      if (resp.ok && (data.code === "SU" || data.code === "SUCCESS")) {
        const refreshed = await refreshAccessToken();
        if (refreshed) {
          setAccessToken(refreshed);
          navigate("/main");
          return;
        } else {
          setErrors({
            general: "자동 로그인(토큰 발급) 실패. 다시 시도하세요.",
          });
          return;
        }
      }

      // 실패 케이스
      setErrors({ general: data.message || "로그인에 실패했습니다." });
    } catch (err) {
      console.error(err);
      setErrors({ general: "서버와 통신 중 오류가 발생했습니다." });
    }
  };

  return (
    <div className={styles.loginContainer}>
      <h2 className={styles.loginTitle}>로그인</h2>
      <form onSubmit={handleSubmit}>
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
          {errors.userId && <p className={styles.errorText}>{errors.userId}</p>}
        </div>

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

        {errors.general && <p className={styles.errorText}>{errors.general}</p>}
        <button type="submit" className={styles.loginButton}>
          로그인
        </button>
      </form>
      <div className={styles.footer}>
        아직 계정이 없으신가요? <a href="/sign-up">회원가입</a>
      </div>
    </div>
  );
};

export default LoginForm;
