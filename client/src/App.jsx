
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Login from './pages/Login'
import { Home } from './pages/Home'
import { AuthInit } from './components/AuthInit'
import { SignUp } from './pages/SignUp'
import { useState } from 'react'
import { Callback } from './pages/Callback'

/**
 * [WHY] 인증 초기화(AuthInit)가 완료된 후에만 라우팅을 렌더링
 *        새로고침 시 AT재발급이 끝나기 전에 Routes가 렌더링되면
 *        인증이 필요한 페이지에서 401이 발생할 수 있기 때문
 */

function App() {
  const [ready, setReady] = useState(false);

  return (
    <BrowserRouter>
      {/* [WHAT] 앱 진입 시 AT 재발급 시도 -> 완료되면 ready=true

          [WHY] onReady를 prop으로 전달하는 이유 :
          ready state를 App이 들고있고
          초기화 완료 시점은 AuthInit가 알고 있으면
          App은 그 결과를 모름
          이를 방지하기위해
          -> App이 콜백을 내려줘서 AuthInit가 완료 시점에 호출하는 구조
      */}
      <AuthInit onReady={() => setReady(true)} />
      {ready && (
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/home" element={<Home />} />
          <Route path="/signUp" element={<SignUp />} />
          <Route path="/callback" element={<Callback />} />
        </Routes>
      )}
    </BrowserRouter>
  )
}

export default App
