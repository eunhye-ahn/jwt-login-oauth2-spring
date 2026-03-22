import { useState } from "react";
import { loginApi } from "../api/loginApi";
import { Link, useNavigate } from "react-router-dom";
import { getAccessToken, setAccessToken } from "../api/axiosInstance";

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const { data } = await loginApi(email, password);
            setAccessToken(data.accessToken);
            navigate('/home');
        } catch (err) {
            console.log(err.response?.data?.message);
            setError(err.response?.data?.message);
        }
    }

    return (
        <div style={{
            //오버레이
            position: "fixed",
            top: 0,
            left: 0,
            width: "100vw",
            height: "100vh",
            // 
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            backgroundColor: "rgba(0,0,0,0.5)"
        }}>
            <div style={{
                backgroundColor: "white",
                padding: "40px",
                borderRadius: "12px",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: "16px",
                width: "360px"
            }}>
                <h1>Login</h1>
                <form onSubmit={handleLogin} style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "12px",
                    width: "100%"
                }}>
                    <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                        <label style={{ fontSize: "14px", color: "#555" }}>Email</label>
                        <input
                            type="email"
                            placeholder="abc1234@naver.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            style={{
                                padding: "10px",
                                borderRadius: "8px",
                                border: "1px solid #ddd",
                                fontSize: "14px"
                            }}
                        />
                    </div>
                    <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                        <label style={{ fontSize: "14px", color: "#555" }}>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            style={{
                                padding: "10px",
                                borderRadius: "8px",
                                border: "1px solid #ddd",
                                fontSize: "14px"
                            }}
                        />
                    </div>
                    {error && <p style={{ color: "red", margin: 0, fontSize: "13px" }}>{error}</p>}
                    <button type="submit" style={{
                        padding: "10px",
                        backgroundColor: "#4A90E2",
                        color: "white",
                        border: "none",
                        borderRadius: "8px",
                        fontSize: "15px",
                        cursor: "pointer",
                        marginTop: "8px"
                    }}>Login</button>
                </form>
                <div>
                    <Link to='/signup'>SignUp</Link>
                </div>
            </div>
        </div>
    )
}

export default Login;