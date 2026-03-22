import api, { getAccessToken, setAccessToken } from "./axiosInstance";

api

/**
 * 로그인
 * @param {string} email
 * @param {string} password
 * @returns {accessToken}
 * @response 200 - Set-Cookie: refreshToken={RT}; HttpOnly (RT 쿠키 발급)
 */

export const loginApi = (email, password) => {
    return api.post('/auth/login', { email, password });
};


/**
 * 유저조회
 * @requires 인증필요(AT)
 * @returns {email, name}
 * 
 */

export const userInfoApi = () => {
    return api.get('/user/userInfo')
}


/**
 * 로그아웃
 * @requires 인증필요(AT)
 * @response 200 - Set-Cookie : refreshToken-; Max-Age=0 (RT 쿠키 삭제)
 */
export const logoutApi = () => {
    return api.post("/auth/logout")
}

/**
 * AT 재발급
 * @requires RT 쿠키 (HttpOnly - 자동전송)
 * @returns {accessToken}
 * @sideEffect 새 AT를 axiosInstance의 메모리 변수에 저장
 */
export const reissueApi = async () => {
    const response = await api.post("/auth/reissue");
    setAccessToken(response.data.accessToken);
    console.log(getAccessToken());
}

/**
 * 회원가입
 * @param {name}
 * @param {email}
 * @param {password}
 */
export const signUpApi = (name, email, password) => {

    return api.post("/auth/signUp", { name, email, password });
}

//api 요청에서 에러를 뱉을시 예외처리를 안해줘서임.