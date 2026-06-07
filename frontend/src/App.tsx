import { Activity, ArrowRight, Building2, LockKeyhole, Mail, ShieldCheck, Stethoscope, UserRound } from 'lucide-react';

const quickAccessItems = [
  {
    label: 'Portal Médico',
    description: 'Agenda, pacientes y atención clínica',
    icon: Stethoscope
  },
  {
    label: 'Portal Administrativo',
    description: 'Gestión operacional del centro',
    icon: Building2
  },
  {
    label: 'Portal Paciente',
    description: 'Información y documentos clínicos',
    icon: UserRound
  }
];

function App() {
  return (
    <main className="login-page">
      <section className="login-page__brand" aria-label="Identidad I-Clinical Technology">
        <div className="login-page__logo-frame">
          <img className="login-page__logo" src="/logo.png" alt="Logo I-Clinical Technology" />
        </div>

        <p className="login-page__eyebrow">Information Clinical Technology</p>
        <h1 className="login-page__title">I-Clinical Technology</h1>
        <p className="login-page__slogan">Conectando información clínica con inteligencia</p>

        <div className="login-page__trust" aria-label="Atributos de la plataforma">
          <span className="login-page__trust-item">
            <Activity aria-hidden="true" size={18} />
            Gestión clínica inteligente
          </span>
          <span className="login-page__trust-item">
            <ShieldCheck aria-hidden="true" size={18} />
            Acceso corporativo seguro
          </span>
        </div>
      </section>

      <section className="login-page__access" aria-label="Acceso a la plataforma">
        <form className="login-card" onSubmit={(event) => event.preventDefault()}>
          <div className="login-card__header">
            <p className="login-card__eyebrow">Bienvenido</p>
            <h2 className="login-card__title">Acceso corporativo</h2>
            <p className="login-card__subtitle">Ingrese sus credenciales para continuar.</p>
          </div>

          <div className="login-card__field">
            <label className="login-card__label" htmlFor="email">Correo electrónico</label>
            <div className="login-card__input-wrap">
              <Mail aria-hidden="true" size={18} />
              <input
                className="login-card__input"
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                placeholder="nombre@iclinical.cl"
              />
            </div>
          </div>

          <div className="login-card__field">
            <label className="login-card__label" htmlFor="password">Contraseña</label>
            <div className="login-card__input-wrap">
              <LockKeyhole aria-hidden="true" size={18} />
              <input
                className="login-card__input"
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                placeholder="Ingrese su contraseña"
              />
            </div>
          </div>

          <div className="login-card__options">
            <label className="login-card__remember" htmlFor="remember">
              <input className="login-card__checkbox" id="remember" name="remember" type="checkbox" />
              Recordarme
            </label>
            <a className="login-card__link" href="#forgot-password">¿Olvidó su contraseña?</a>
          </div>

          <button className="login-card__submit" type="submit">
            Iniciar Sesión
            <ArrowRight aria-hidden="true" size={18} />
          </button>
        </form>

        <div className="quick-access" aria-label="Accesos rápidos">
          {quickAccessItems.map((item) => {
            const Icon = item.icon;

            return (
              <button className="quick-access__item" key={item.label} type="button">
                <span className="quick-access__icon">
                  <Icon aria-hidden="true" size={20} />
                </span>
                <span className="quick-access__copy">
                  <span className="quick-access__label">{item.label}</span>
                  <span className="quick-access__description">{item.description}</span>
                </span>
              </button>
            );
          })}
        </div>
      </section>
    </main>
  );
}

export default App;
