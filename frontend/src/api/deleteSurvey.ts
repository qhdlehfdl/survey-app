import axios from "axios";
import { refreshAccessToken } from "./refreshToken";

export async function deleteSurvey(surveyId: number): Promise<void> {
  const makeRequest = async (token: string | null) => {
    const config = {
      headers: token
        ? {
            Authorization: `Bearer ${token}`,
            Accept: "application/json",
          }
        : { Accept: "application/json" },
      withCredentials: true as const,
    };

    return axios.delete(`/api/survey/${surveyId}`, config);
  };

  let accessToken = localStorage.getItem("accessToken");

  try {
    await makeRequest(accessToken);
  } catch (err: any) {
    const respData = err?.response?.data;
    const status = err?.response?.status;

    const needRefresh =
      respData?.code === "NP" || status === 401 || status === 403;

    if (!needRefresh) {
      throw err;
    }

    // 토큰 갱신
    const newToken = await refreshAccessToken();
    if (!newToken) throw new Error("토큰 갱신에 실패했습니다.");
    localStorage.setItem("accessToken", newToken);

    // 재요청
    await makeRequest(newToken);
  }
}
