import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { reissueApi } from "../api/loginApi";
import { getAccessToken } from "../api/axiosInstance";

/**
 * [WHAT] 앱 최초 진입 시 인증 상태 초기화 컴포넌트
 * [WHY]  새로고침 시 메모리에 저장된 AT가 사라짐
 *        RT(쿠키)는 살아있으므로 앱 시작 시 AT 재발급 시도
 *        성공 시 → 그대로 진입
 *        실패 시 → 로그인 페이지로 이동
 *
 * [WHY]  null을 반환하는 이유 :
 *        UI를 렌더링하는 컴포넌트가 아니라
 *        인증 초기화 로직만 담당하는 컴포넌트이기 때문
 * 
 */

export const AuthInit = ({ onReady }) => {
    const navigate = useNavigate();

    useEffect(() => {
        const initAuth = async () => {
            if (getAccessToken()) {
                onReady();
                return;
            }

            try {
                await reissueApi();
            } catch (e) {
                navigate('/');
            } finally {
                onReady();
            }
        };
        initAuth();
    }, []);

    return;
}