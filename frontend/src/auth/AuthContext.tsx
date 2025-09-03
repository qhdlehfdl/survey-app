// src/auth/AuthContext.tsx
import React, {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
  useCallback,
} from "react";
import { useNavigate } from "react-router-dom";
import { refreshAccessToken } from "../api/refreshToken";

type AuthContextType = {
  isAuthenticated: boolean;
  accessToken: string | null;
  setAccessToken: (t: string | null) => void;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}

function parseJwt(token: string | null) {
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload;
  } catch {
    return null;
  }
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [accessToken, setAccessTokenState] = useState<string | null>(() => {
    return localStorage.getItem("accessToken");
  });
  const navigate = useNavigate();

  // single-flight refresh 방지
  const refreshPromiseRef = useRef<Promise<boolean> | null>(null);
  const initializedRef = useRef(false);

  function setAccessToken(t: string | null) {
    setAccessTokenState(t);
    if (t) {
      try {
        localStorage.setItem("accessToken", t);
      } catch (e) {
        console.warn("localStorage set failed:", e);
      }
    } else {
      try {
        localStorage.removeItem("accessToken");
      } catch (e) {
        console.warn("localStorage remove failed:", e);
      }
    }
  }

  async function callRefreshOnce(): Promise<boolean> {
    if (refreshPromiseRef.current) return refreshPromiseRef.current;

    const p = (async () => {
      try {
        const newAccess = await refreshAccessToken(); // axios 기반 함수 호출
        if (newAccess) {
          setAccessToken(newAccess);
          return true;
        }
        return false;
      } catch (err) {
        console.error("refresh error", err);
        return false;
      } finally {
        // promise를 해제 (다음번 호출을 허용)
        setTimeout(() => {
          refreshPromiseRef.current = null;
        }, 0);
      }
    })();

    refreshPromiseRef.current = p;
    return p;
  }

  useEffect(() => {
    if (initializedRef.current) return;
    initializedRef.current = true;

    (async () => {
      // accessToken이 없으면 refresh 시도
      if (!accessToken) {
        await callRefreshOnce();
        return;
      }
      // 있으면 만료 여부 검사
      const payload = parseJwt(accessToken);
      const now = Math.floor(Date.now() / 1000);
      const exp = payload?.exp ?? 0;
      if (exp <= now + 5) {
        await callRefreshOnce();
      }
    })();
    // 빈 배열 -> 루트에 한 번 마운트될 때만 실행
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const logout = useCallback(async () => {
    try {
      await fetch("/api/auth/logout", {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          Authorization: accessToken ? `Bearer ${accessToken}` : "",
        },
      });
    } catch (err) {
      console.error("logout error", err);
    } finally {
      setAccessToken(null);
      // 이동: sign-in으로 보내고 싶으면 "/sign-in"으로 바꾸세요
      navigate("/main");
    }
  }, [accessToken, navigate]);

  // 전역 이벤트 리스너: refresh 실패(IR T 등) 시 auth:refreshInvalid 이벤트를 받아 logout 호출
  useEffect(() => {
    if (typeof window === "undefined" || !window.addEventListener) return;

    const handler = (evt: Event) => {
      // 로그 남기기
      console.warn("auth:refreshInvalid event received, calling logout()");
      // 비동기 호출
      (async () => {
        try {
          await logout();
        } catch (e) {
          console.error("logout from event failed", e);
        }
      })();
    };

    window.addEventListener("auth:refreshInvalid", handler);

    return () => {
      window.removeEventListener("auth:refreshInvalid", handler);
    };
  }, [logout]);

  const value: AuthContextType = {
    isAuthenticated: !!accessToken,
    accessToken,
    setAccessToken,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
