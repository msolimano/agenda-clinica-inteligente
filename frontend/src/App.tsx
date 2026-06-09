import { useEffect, useState } from 'react';
import { Activity, ArrowRight, Building2, LockKeyhole, Mail, ShieldCheck, Stethoscope, UserRound } from 'lucide-react';
import { ProfessionalsPage } from './modules/professionals/ProfessionalsPage';
import { PatientsPage } from './modules/patients/PatientsPage';
import { AppointmentsPage } from './modules/appointments/AppointmentsPage';
import { WaitingListPage } from './modules/waiting-list/WaitingListPage';
import { AgendaControlsPage } from './modules/agenda-controls/AgendaControlsPage';
import { DocumentsPage } from './modules/documents/DocumentsPage';
import { ClinicalRecordsPage } from './modules/clinical-records/ClinicalRecordsPage';
import { MedicationCatalogPage } from './modules/medications/MedicationCatalogPage';
import { DiagnosisCatalogPage } from './modules/diagnosis-catalog/DiagnosisCatalogPage';

const PROFESSIONALS_ROUTE = '#/professionals';
const PATIENTS_ROUTE = '#/patients';
const APPOINTMENTS_ROUTE = '#/appointments';
const WAITING_LIST_ROUTE = '#/waiting-list';
const AGENDA_CONTROLS_ROUTE = '#/agenda-controls';
const DOCUMENTS_ROUTE = '#/documents';
const CLINICAL_RECORDS_ROUTE = '#/clinical-records';
const MEDICATIONS_ROUTE = '#/medications';
const DIAGNOSIS_CATALOG_ROUTE = '#/diagnosis-catalog';

const quickAccessItems = [
  {
    label: 'Portal Médico',
    description: 'Agenda, pacientes y atención clínica',
    icon: Stethoscope,
    targetHash: APPOINTMENTS_ROUTE
  },
  {
    label: 'Portal Administrativo',
    description: 'Gestión operacional del centro',
    icon: Building2,
    targetHash: PROFESSIONALS_ROUTE
  },
  {
    label: 'Portal Paciente',
    description: 'Información y documentos clínicos',
    icon: UserRound,
    targetHash: PATIENTS_ROUTE
  }
];

function App() {
  const [route, setRoute] = useState(window.location.hash);

  useEffect(() => {
    function handleHashChange() {
      setRoute(window.location.hash);
    }

    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  if (route === PROFESSIONALS_ROUTE) {
    return <ProfessionalsPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

  if (route === APPOINTMENTS_ROUTE) {
    return <AppointmentsPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

  if (route === WAITING_LIST_ROUTE) {
    return <WaitingListPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

  if (route === AGENDA_CONTROLS_ROUTE) {
    return <AgendaControlsPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

  if (route === DOCUMENTS_ROUTE) {
    return <DocumentsPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

  if (route === CLINICAL_RECORDS_ROUTE) {
    return <ClinicalRecordsPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

  if (route === MEDICATIONS_ROUTE) {
    return <MedicationCatalogPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

  if (route === DIAGNOSIS_CATALOG_ROUTE) {
    return <DiagnosisCatalogPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

  if (route === PATIENTS_ROUTE) {
    return <PatientsPage onBackToLogin={() => { window.location.hash = ''; }} />;
  }

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
              <button
                className="quick-access__item"
                key={item.label}
                type="button"
                onClick={() => {
                  if (item.targetHash) {
                    window.location.hash = item.targetHash;
                  }
                }}
              >
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
