// ProfilePage.tsx
import { useCallback, useEffect, useRef, useState } from "react";
import { getMyProfile } from "../../api/getMyProfile"; // lastId 기반
import styles from "./ProfilePage.module.css";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext"; // 추가
import { deleteSurvey } from "../../api/deleteSurvey";

type Survey = {
  id: number;
  title: string;
  participantsNum: number;
  writerNickname?: string;
};

const PAGE_SIZE = 5;

export default function ProfilePage() {
  const [nickname, setNickname] = useState("");
  const [surveys, setSurveys] = useState<Survey[]>([]);
  const [lastId, setLastId] = useState<number>(0); // 초기 lastId
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const isLoadingRef = useRef(false);
  const seenIdsRef = useRef<Set<number>>(new Set());
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth(); // 추가

  const disconnectObserver = () => {
    if (observerRef.current) {
      observerRef.current.disconnect();
      observerRef.current = null;
    }
  };

  // 로그아웃 핸들러 추가
  const handleLogout = async () => {
    try {
      await logout();
      alert("로그아웃 되었습니다.");
      navigate("/sign-in");
    } catch (err) {
      console.error("logout failed", err);
      alert("로그아웃 실패");
    }
  };

  // lastId를 인자로 받아서 fetch
  const fetchProfilePage = useCallback(
    async (cursor: number) => {
      if (isLoadingRef.current || !hasMore) return;

      isLoadingRef.current = true;
      setLoading(true);
      setError("");

      try {
        const token = localStorage.getItem("accessToken");
        const data = await getMyProfile(token, cursor, PAGE_SIZE);

        // 첫 로드에서 nickname 설정
        if (data.nickname && cursor === 0) setNickname(data.nickname);

        const received: Survey[] = data.surveys ?? [];
        const newItems = received.filter(
          (it) => !seenIdsRef.current.has(it.id)
        );

        setSurveys((prev) => {
          const next = [...prev, ...newItems];
          newItems.forEach((it) => seenIdsRef.current.add(it.id));
          return next;
        });

        // 다음 커서 및 hasMore 업데이트
        if (data.hasNext && data.nextCursor != null) {
          setLastId(data.nextCursor);
          setHasMore(true);
        } else {
          setHasMore(false);
          disconnectObserver();
        }
      } catch (err: any) {
        console.error("fetchProfilePage error:", err);
        setError("데이터를 불러오는 중 오류가 발생했습니다.");
        setHasMore(false);
        disconnectObserver();
      } finally {
        isLoadingRef.current = false;
        setLoading(false);
      }
    },
    [hasMore]
  );

  const handleDeleteSurvey = async (surveyId: number) => {
    if (!window.confirm("정말 이 설문을 삭제하시겠습니까?")) return;

    try {
      await deleteSurvey(surveyId);

      // 삭제 후 UI에서 바로 제거
      setSurveys((prev) => prev.filter((s) => s.id !== surveyId));
      seenIdsRef.current.delete(surveyId);
      alert("설문이 삭제되었습니다.");
    } catch (err) {
      console.error("삭제 실패:", err);
      alert("설문 삭제에 실패했습니다.");
    }
  };

  // 초기 로드 (한 번만 실행)
  useEffect(() => {
    setLastId(0);
    setHasMore(true);
    seenIdsRef.current.clear();
    fetchProfilePage(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // [] -> 한 번만 실행

  // IntersectionObserver 설정
  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel || !hasMore) return;

    if (observerRef.current) {
      observerRef.current.disconnect();
      observerRef.current = null;
    }

    observerRef.current = new IntersectionObserver(
      (entries) => {
        const e = entries[0];
        if (!e.isIntersecting || isLoadingRef.current || !hasMore) return;

        fetchProfilePage(lastId);
      },
      { root: null, rootMargin: "200px", threshold: 0.1 }
    );

    observerRef.current.observe(sentinel);

    return () => disconnectObserver();
  }, [hasMore, lastId, fetchProfilePage]);

  return (
    <div className={styles.container}>
      {/* 홈 버튼: 왼쪽 상단 (절대 위치) */}
      <button
        className={styles.homeBtn}
        aria-label="홈으로"
        onClick={() => navigate("/main")}
        title="홈으로"
      >
        {/* simple home SVG icon */}
        <svg
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
          focusable="false"
        >
          <path
            d="M3 11.5L12 4l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1V11.5z"
            stroke="currentColor"
            strokeWidth="1.2"
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="currentColor"
          />
        </svg>
      </button>

      <header className={styles.header}>
        <h1 className={styles.greeting}>
          {nickname ? `${nickname}님 환영합니다 👋` : "로딩 중..."}
        </h1>
        <div className={styles.headerRight}>
          {/* 로그인 상태에 따라 로그아웃 또는 로그인 버튼 보여주기 */}
          {isAuthenticated ? (
            <button className={styles.logoutBtn} onClick={handleLogout}>
              로그아웃
            </button>
          ) : (
            <button
              className={styles.logoutBtn}
              onClick={() => navigate("/sign-in")}
            >
              로그인
            </button>
          )}
        </div>
      </header>

      <main className={styles.main}>
        {error && <div className={styles.error}>{error}</div>}
        {!error && surveys.length === 0 && !loading && (
          <div className={styles.message}>등록한 설문이 없습니다.</div>
        )}

        <div className={styles.surveyList}>
          {surveys.map((s) => (
            <div key={s.id} className={styles.surveyCard}>
              <div className={styles.cardHeader}>
                <h2 className={styles.cardTitle}>{s.title}</h2>
                <span className={styles.participants}>
                  참여자 {s.participantsNum}명
                </span>
              </div>

              <div className={styles.cardActions}>
                <button
                  className={styles.editBtn}
                  onClick={() =>
                    navigate(`/survey/${s.id}/edit`, {
                      state: { participantsNum: s.participantsNum },
                    })
                  }
                >
                  설문지 수정
                </button>

                {/* 기존 결과 보기 버튼 */}
                <button
                  className={styles.viewBtn}
                  onClick={() => navigate(`/survey/${s.id}/result`)}
                >
                  결과 보기
                </button>
                <button
                  className={styles.deleteBtn}
                  onClick={() => handleDeleteSurvey(s.id)}
                >
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>

        {loading && <div className={styles.loadingMore}>불러오는 중</div>}
        {!hasMore && surveys.length > 0 && (
          <div className={styles.endMessage}>마지막 설문입니다 🎉</div>
        )}

        <div ref={sentinelRef} className={styles.sentinel}></div>
      </main>
    </div>
  );
}
