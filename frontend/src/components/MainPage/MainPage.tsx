import React, { useState, useEffect, useRef } from "react";
import styles from "./MainPage.module.css";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

type Survey = {
  id: number;
  title: string;
  description?: string;
  writerNickname?: string;
  participantsNum?: number;
};

export default function MainPage() {
  const [page, setPage] = useState<number>(1);
  const perPage = 9;

  const [surveys, setSurveys] = useState<Survey[]>([]);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalElements, setTotalElements] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const retryRef = useRef(0);
  const MAX_RETRIES = 3;
  const BASE_BACKOFF_MS = 800;
  const timeoutRef = useRef<number | null>(null);
  const API_BASE_URL = "http://3.107.238.215:8080";

  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  useEffect(() => {
    let aborted = false;
    const controller = new AbortController();
    const signal = controller.signal;

    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }

    async function fetchPageOnce() {
      setLoading(true);
      setError(null);

      try {
        const res = await fetch(`/api/survey?page=${page}&size=${perPage}`, {
          method: "GET",
          headers: { Accept: "application/json" },
          signal,
        });

        if (!res.ok) {
          if (res.status >= 500 && retryRef.current < MAX_RETRIES) {
            scheduleRetry();
            return;
          }
          const txt = await res.text().catch(() => "");
          setError(`서버 응답 오류: ${res.status} ${txt}`);
          return;
        }

        const data = await res.json();

        if (data?.code && data.code !== "SU" && data.code !== "SUCCESS") {
          if (retryRef.current < MAX_RETRIES) {
            scheduleRetry();
            return;
          }
          setError(data.message ?? "서버 에러가 발생했습니다.");
          console.error("API error:", data);
          return;
        }

        if (aborted) return;

        const itemsRaw: any[] = data.surveys ?? data.items ?? [];
        const items: Survey[] = itemsRaw.map((it) => ({
          id: it.id,
          title: it.title,
          description: it.description ?? it.summary ?? "",
          writerNickname: it.writerNickname ?? it.writer?.nickname ?? "",
          participantsNum:
            typeof it.participantsNum === "number" ? it.participantsNum : 0,
        }));

        const tPages: number | undefined = data.totalPages ?? data.total_pages;
        const tElements: number | undefined =
          data.totalElements ?? data.total_elements ?? data.total;

        setSurveys(items);
        if (typeof tPages === "number") setTotalPages(tPages);
        else if (typeof tElements === "number")
          setTotalPages(Math.max(1, Math.ceil(tElements / perPage)));
        else setTotalPages(Math.max(1, Math.ceil(items.length / perPage)));

        if (typeof tElements === "number") setTotalElements(tElements);
        else setTotalElements(null);

        retryRef.current = 0;
      } catch (err: any) {
        if (err.name === "AbortError") return;
        console.error("fetchPage error", err);
        if (retryRef.current < MAX_RETRIES) {
          scheduleRetry();
          return;
        }
        setError("데이터를 불러오는 중 네트워크 오류가 발생했습니다.");
      } finally {
        if (!aborted) setLoading(false);
      }
    }

    function scheduleRetry() {
      const next = retryRef.current + 1;
      retryRef.current = next;
      const backoff = BASE_BACKOFF_MS * Math.pow(2, next - 1);
      timeoutRef.current = window.setTimeout(() => {
        if (!signal.aborted) fetchPageOnce();
      }, backoff) as unknown as number;
    }

    fetchPageOnce();

    return () => {
      aborted = true;
      controller.abort();
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
        timeoutRef.current = null;
      }
    };
  }, [page]); // page 변경 시마다 호출

  const handleLogout = async () => {
    try {
      await logout();
      alert("로그아웃 되었습니다.");
    } catch (err) {
      console.error(err);
      alert("로그아웃 실패");
    }
  };

  const handleProfileClick = () => {
    if (isAuthenticated) navigate("/profile");
    else navigate("/sign-in");
  };

  const handlePrev = () => setPage((p) => Math.max(p - 1, 1));
  const handleNext = () => setPage((p) => Math.min(p + 1, totalPages));
  const goToPage = (p: number) => setPage(Math.max(1, Math.min(p, totalPages)));

  // helper: render simple page list; if totalPages is large you can replace with windowing
  const renderPageButtons = () => {
    if (totalPages <= 1) return null;

    // Simple full list (1..totalPages)
    return (
      <div className={styles.pageList}>
        {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
          <button
            key={p}
            className={`${styles.pageButton} ${
              p === page ? styles.pageButtonActive : ""
            }`}
            onClick={() => goToPage(p)}
            aria-current={p === page ? "page" : undefined}
          >
            {p}
          </button>
        ))}
      </div>
    );
  };

  return (
    <div className={styles.mainContainer}>
      <header className={styles.header}>
        <h1>📋 설문조사 관리</h1>

        {/* 오른쪽 버튼 그룹: 프로필 아이콘 + 로그인/로그아웃 */}
        <div className={styles.headerButtons}>
          <button
            className={styles.profileBtn}
            onClick={handleProfileClick}
            aria-label="내 프로필"
            title={isAuthenticated ? "내 프로필" : "로그인 후 이용하세요"}
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              className={styles.profileIcon}
              role="img"
              aria-hidden="true"
              focusable="false"
            >
              <path d="M12 12c2.761 0 5-2.239 5-5s-2.239-5-5-5-5 2.239-5 5 2.239 5 5 5zM12 14c-4.418 0-8 2.239-8 5v1h16v-1c0-2.761-3.582-5-8-5z" />
            </svg>
          </button>

          {isAuthenticated ? (
            <button onClick={handleLogout} className={styles.logoutBtn}>
              로그아웃
            </button>
          ) : (
            <button
              onClick={() => navigate("/sign-in")}
              className={styles.logoutBtn}
            >
              로그인
            </button>
          )}
        </div>
      </header>

      {loading ? (
        <div className={styles.message}>로딩 중...</div>
      ) : error ? (
        <div className={styles.error}>{error}</div>
      ) : (
        <>
          <div className={styles.surveyGrid}>
            {surveys.length === 0 ? (
              <div className={styles.message}>등록된 설문조사가 없습니다.</div>
            ) : (
              surveys.map((survey) => (
                <div key={survey.id} className={styles.surveyCard}>
                  <div className={styles.cardHeader}>
                    <div className={styles.cardMain}>
                      <h2 className={styles.cardTitle}>{survey.title}</h2>
                      <div className={styles.cardMeta}>
                        작성자: {survey.writerNickname ?? "알 수 없음"}
                      </div>
                    </div>

                    <div
                      className={styles.participantBadge}
                      aria-label={`참여자 수 ${survey.participantsNum ?? 0}`}
                    >
                      <div className={styles.participantLabel}>참여자</div>
                      <div className={styles.participantValue}>
                        {survey.participantsNum ?? 0}
                      </div>
                    </div>
                  </div>

                  <p className={styles.cardDesc}>{survey.description ?? ""}</p>

                  <div className={styles.cardActions}>
                    <button
                      className={styles.viewBtn}
                      onClick={() => navigate(`/survey/${survey.id}`)}
                    >
                      보기
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Pagination */}
          <div className={styles.pagination}>
            <button onClick={handlePrev} disabled={page === 1}>
              이전
            </button>

            {/* PAGE NUMBER BUTTONS */}
            {renderPageButtons()}

            <button onClick={handleNext} disabled={page === totalPages}>
              다음
            </button>
          </div>
        </>
      )}

      <div className={styles.bottomAction}>
        <button className={styles.createBtn} onClick={() => navigate("/form")}>
          ➕ 설문지 작성
        </button>
      </div>
    </div>
  );
}
