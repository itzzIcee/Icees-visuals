import { Navigate, Route, Routes } from 'react-router-dom'
import { Landing } from './pages/Landing'
import { Studio } from './pages/Studio'
import './App.css'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Landing />} />
      <Route path="/studio" element={<Studio />} />
      <Route path="/studio/:tool" element={<Studio />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
