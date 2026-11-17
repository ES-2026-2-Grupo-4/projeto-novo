import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/useAuth.ts";
import { TrophyIcon, LogOutIcon, UserIcon, ShieldIcon } from "../common/Icons.tsx";

const ROLE_LABELS: Record<string, string> = {
  ORGANIZADOR: "Organizador",
  DIRIGENTE: "Dirigente",
  ARBITRO: "Árbitro",
  ESPECTADOR: "Espectador",
};

export function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const getInitials = (name?: string) => {
    if (!name) return "U";
    return name
      .trim()
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((n) => n[0].toUpperCase())
      .join("");
  };

  return (
    <header className="navbar-container">
      <div className="navbar-inner">
        <Link to="/dashboard" className="navbar-brand">
          <div className="navbar-brand-icon">
            <TrophyIcon />
          </div>
          <span className="navbar-brand-text">Arena UFCG</span>
        </Link>

        <nav className="navbar-links">
          <Link to="/dashboard" className="navbar-link">
            Dashboard
          </Link>
          <Link to="/campeonatos" className="navbar-link">
            Campeonatos
          </Link>
          <Link to="/perfil" className="navbar-link">
            Meu Perfil
          </Link>
        </nav>

        <div className="navbar-user-section">
          {user && (
            <div className="navbar-user-profile">
              <div className="navbar-avatar">{getInitials(user.nome)}</div>
              <div className="navbar-user-info">
                <span className="navbar-user-name">{user.nome}</span>
                <span className={`navbar-role-badge role-${user.perfil.toLowerCase()}`}>
                  <ShieldIcon className="role-icon" />
                  {ROLE_LABELS[user.perfil] || user.perfil}
                </span>
              </div>
            </div>
          )}

          <div className="navbar-actions">
            <Link to="/perfil" className="btn btn-ghost btn-sm" title="Ver perfil">
              <UserIcon />
            </Link>
            <button
              type="button"
              onClick={handleLogout}
              className="btn btn-outline btn-sm btn-logout"
              title="Encerrar sessão"
            >
              <LogOutIcon />
              <span>Sair</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
