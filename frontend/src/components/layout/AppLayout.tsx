import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar.tsx';

export function AppLayout() {
  return (
    <div className="app-layout-wrapper">
      <Navbar />
      <main className="app-main-container">
        <Outlet />
      </main>
      <footer className="app-footer">
        <div className="app-footer-inner">
          <p>&copy; {new Date().getFullYear()} Arena UFCG &bull; Sistema Esportivo Integrado UFCG</p>
          <div className="app-footer-links">
            <span className="app-footer-badge">Plataforma Web</span>
          </div>
        </div>
      </footer>
    </div>
  );
}
