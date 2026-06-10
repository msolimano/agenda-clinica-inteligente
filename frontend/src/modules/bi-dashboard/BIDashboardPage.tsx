import { ArrowLeft, BarChart3 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { listProfessionals, listSpecialties } from '../professionals/professionalsApi';
import type { Professional, Specialty } from '../professionals/professionals.types';
import { AgendaKpiPanel } from './AgendaKpiPanel';
import { AIKpiPanel } from './AIKpiPanel';
import { BIDashboardFilters } from './BIDashboardFilters';
import { ClinicalKpiPanel } from './ClinicalKpiPanel';
import { DocumentsKpiPanel } from './DocumentsKpiPanel';
import { FHIRKpiPanel } from './FHIRKpiPanel';
import { SummaryKpiCards } from './SummaryKpiCards';
import { defaultFromDate, defaultToDate, endOfDate, shortDate, startOfDate } from './biDashboardDate';
import { getBISummary } from './biDashboardApi';
import type { BISummary } from './biDashboard.types';
import './bi-dashboard.css';

interface BIDashboardPageProps {
  onBackToLogin: () => void;
}

export function BIDashboardPage({ onBackToLogin }: BIDashboardPageProps) {
  const [summary, setSummary] = useState<BISummary | null>(null);
  const [professionals, setProfessionals] = useState<Professional[]>([]);
  const [specialties, setSpecialties] = useState<Specialty[]>([]);
  const [fromDate, setFromDate] = useState(defaultFromDate());
  const [toDate, setToDate] = useState(defaultToDate());
  const [professionalId, setProfessionalId] = useState('');
  const [specialtyId, setSpecialtyId] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadCatalogs() {
    try {
      const [professionalsResponse, specialtiesResponse] = await Promise.all([
        listProfessionals({ includeInactive: false }),
        listSpecialties()
      ]);
      setProfessionals(professionalsResponse.filter((professional) => professional.status === 'active'));
      setSpecialties(specialtiesResponse.filter((specialty) => specialty.status === 'active'));
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar filtros');
    }
  }

  async function loadSummary() {
    setIsLoading(true);
    setError(null);
    try {
      const response = await getBISummary({
        from: startOfDate(fromDate),
        to: endOfDate(toDate),
        professionalId,
        specialtyId
      });
      setSummary(response);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar indicadores BI');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadCatalogs();
  }, []);

  useEffect(() => {
    void loadSummary();
  }, []);

  return (
    <main className="bi-dashboard-page">
      <header className="bi-dashboard-page__header">
        <button className="bi-dashboard-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="bi-dashboard-page__heading">
          <span className="bi-dashboard-page__mark"><BarChart3 aria-hidden="true" size={24} /></span>
          <div>
            <p className="bi-dashboard-page__eyebrow">I-Clinical Technology</p>
            <h1 className="bi-dashboard-page__title">BI Clinico Operacional</h1>
          </div>
        </div>
        <div className="bi-dashboard-page__range">
          {summary ? <span>{shortDate(summary.from)} - {shortDate(summary.to)}</span> : <span>Ultimos 30 dias</span>}
        </div>
      </header>

      <BIDashboardFilters
        fromDate={fromDate}
        toDate={toDate}
        professionalId={professionalId}
        specialtyId={specialtyId}
        professionals={professionals}
        specialties={specialties}
        isLoading={isLoading}
        onFromDateChange={setFromDate}
        onToDateChange={setToDate}
        onProfessionalChange={setProfessionalId}
        onSpecialtyChange={setSpecialtyId}
        onRefresh={() => void loadSummary()}
      />

      {error ? <div className="bi-dashboard-page__alert" role="alert">{error}</div> : null}
      {isLoading ? <div className="bi-dashboard-page__loading">Cargando indicadores...</div> : null}

      {summary ? (
        <>
          <SummaryKpiCards summary={summary} />
          <section className="bi-dashboard-page__content">
            <AgendaKpiPanel agenda={summary.agenda} waitingList={summary.waitingList} patients={summary.patients} />
            <ClinicalKpiPanel clinical={summary.clinical} />
            <DocumentsKpiPanel documents={summary.documents} />
            <AIKpiPanel ai={summary.ai} />
            <FHIRKpiPanel fhir={summary.fhir} />
          </section>
        </>
      ) : null}
    </main>
  );
}
