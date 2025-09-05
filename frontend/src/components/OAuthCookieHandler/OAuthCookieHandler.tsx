// src/pages/OAuthCookieHandler.tsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext"; 
import { refreshAccessToken } from "../../api/refreshToken"; // 제공하신 함수 사용

export default function OAuthCookieHandler() {
  const navigate = useNavigate();
  const { setAccessToken } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;

    (async () => {
      try {
        setLoading(true);
        // refreshAccessToken() 내부에서 withCredentials: true 로 호출하므로
        // 브라우저가 HttpOnly refresh 쿠키를 자동 포함합니다.
        const newToken = await refreshAccessToken();
          console.log(newToken);
        if (!mounted) return;

        if (newToken) {
          setAccessToken(newToken);
          navigate("/main", { replace: true });
          return;
        } else {
          // refresh 실패: refreshAccessToken()이 이미 전역 이벤트를 발생시켰을 수 있음
          setError(
            "자동 로그인(토큰 재발급)에 실패했습니다. 다시 시도해 주세요."
          );
          // 선택적으로 바로 로그인 페이지로 보낼 수 있습니다:
          navigate("/sign-in", { replace: true });
        }
      } catch (err: any) {
        console.error("OAuthCookieHandler error:", err);
        setError("로그인 처리 중 오류가 발생했습니다.");
        navigate("/sign-in", { replace: true });
      } finally {
        if (mounted) setLoading(false);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [navigate, setAccessToken]);

  if (loading) {
    return (
      <div style={{ padding: 24 }}>
        <p>로그인 정보를 처리하는 중입니다… 잠시만 기다려 주세요.</p>
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      {error ? (
        <>
          <p style={{ color: "red" }}>{error}</p>
          <p>
            문제가 계속되면 <a href="/sign-in">로그인 페이지</a>로 이동하여 수동
            로그인해 주세요.
          </p>
        </>
      ) : null}
    </div>
  );
}
