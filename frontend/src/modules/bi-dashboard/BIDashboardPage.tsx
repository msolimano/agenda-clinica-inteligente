import { ArrowLeft, BarChart3 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { listProfessionals, listSpecialties } from '../professionals/professionalsApi';
import type { Professional, Specialty } from '../professionals/professionals.types';
import { AgendaKpiPanel } from './AgendaKpiPanel';
import { AIKpiPanel } from './AIKpiPanel';
import { AIStatusChartPanel } from './AIStatusChartPanel';
import { AppointmentTrendPanel } from './AppointmentTrendPanel';
import { BIComparisonPanel } from './BIComparisonPanel';
import { BIDashboardFilters } from './BIDashboardFilters';
import { ClinicalKpiPanel } from './ClinicalKpiPanel';
import { DocumentsKpiPanel } from './DocumentsKpiPanel';
import { EfficiencyKpiPanel } from './EfficiencyKpiPanel';
import { FHIRKpiPanel } from './FHIRKpiPanel';
import { ProfessionalRankingPanel } from './ProfessionalRankingPanel';
import { SpecialtyOccupancyPanel } from './SpecialtyOccupancyPanel';
import { SpecialtyRankingPanel } from './SpecialtyRankingPanel';
import { SummaryKpiCards } from './SummaryKpiCards';
import { TopDiagnosesChartPanel } from './TopDiagnosesChartPanel';
import { TopMedicationsChartPanel } from './TopMedicationsChartPanel';
import { defaultFromDate, defaultToDate, endOfDate, shortDate, startOfDate } from './biDashboardDate';
import {
  getBIAITrends,
  getBIAppointmentTrend,
  getBIComparison,
  getBIEfficiency,
  getBIProfessionalRankings,
  getBISpecialtyOccupancy,
  getBISpecialtyRankings,
  getBISummary,
  getBITopDiagnoses,
  getBITopMedications
} from './biDashboardApi';
import type {
  BIAITrend,
  BIAppointmentTrend,
  BIBarItem,
  BIComparison,
  BIDashboardFilters as BIDashboardFilterValues,
  BIEfficiency,
  BIProfessionalRanking,
  BISpecialtyOccupancy,
  BISpecialtyRanking,
  BISummary
} from './biDashboard.types';
import './bi-dashboard.css';

interface BIDashboardPageProps {
  onBackToLogin: () => void;
}

export function BIDashboardPage({ onBackToLogin }: BIDashboardPageProps) {
  const [summary, setSummary] = useState<BISummary | null>(null);
  const [appointmentTrend, setAppointmentTrend] = useState<BIAppointmentTrend | null>(null);
  const [specialtyOccupancy, setSpecialtyOccupancy] = useState<BISpecialtyOccupancy[]>([]);
  const [topDiagnoses, setTopDiagnoses] = useState<BIBarItem[]>([]);
  const [topMedications, setTopMedications] = useState<BIBarItem[]>([]);
  const [aiTrend, setAiTrend] = useState<BIAITrend | null>(null);
  const [comparison, setComparison] = useState<BIComparison | null>(null);
  const [specialtyRankings, setSpecialtyRankings] = useState<BISpecialtyRanking[]>([]);
  const [professionalRankings, setProfessionalRankings] = useState<BIProfessionalRanking[]>([]);
  const [efficiency, setEfficiency] = useState<BIEfficiency | null>(null);
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

  async function loadDashboard() {
    setIsLoading(true);
    setError(null);
    const filters: BIDashboardFilterValues = {
      from: startOfDate(fromDate),
      to: endOfDate(toDate),
      professionalId,
      specialtyId
    };
    try {
      const [
        summaryResponse,
        appointmentTrendResponse,
        specialtyOccupancyResponse,
        topDiagnosesResponse,
        topMedicationsResponse,
        aiTrendResponse,
        comparisonResponse,
        specialtyRankingsResponse,
        professionalRankingsResponse,
        efficiencyResponse
      ] = await Promise.all([
        getBISummary(filters),
        getBIAppointmentTrend(filters),
        getBISpecialtyOccupancy(filters),
        getBITopDiagnoses(filters),
        getBITopMedications(filters),
        getBIAITrends({ from: filters.from, to: filters.to }),
        getBIComparison(filters),
        getBISpecialtyRankings({ from: filters.from, to: filters.to }),
        getBIProfessionalRankings({ from: filters.from, to: filters.to, specialtyId }),
        getBIEfficiency(filters)
      ]);
      setSummary(summaryResponse);
      setAppointmentTrend(appointmentTrendResponse);
      setSpecialtyOccupancy(specialtyOccupancyResponse);
      setTopDiagnoses(topDiagnosesResponse);
      setTopMedications(topMedicationsResponse);
      setAiTrend(aiTrendResponse);
      setComparison(comparisonResponse);
      setSpecialtyRankings(specialtyRankingsResponse);
      setProfessionalRankings(professionalRankingsResponse);
      setEfficiency(efficiencyResponse);
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
    void loadDashboard();
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
        onRefresh={() => void loadDashboard()}
      />

      {error ? <div className="bi-dashboard-page__alert" role="alert">{error}</div> : null}
      {isLoading ? <div className="bi-dashboard-page__loading">Cargando indicadores...</div> : null}

      {summary ? (
        <>
          <SummaryKpiCards summary={summary} />
          <section className="bi-dashboard-page__advanced" aria-label="BI avanzado operacional">
            <BIComparisonPanel comparison={comparison} />
            <EfficiencyKpiPanel efficiency={efficiency} />
            <SpecialtyRankingPanel items={specialtyRankings} />
            <ProfessionalRankingPanel items={professionalRankings} />
          </section>
          <section className="bi-dashboard-page__visuals" aria-label="Visualizaciones BI">
            <AppointmentTrendPanel trend={appointmentTrend} />
            <SpecialtyOccupancyPanel items={specialtyOccupancy} />
            <TopDiagnosesChartPanel items={topDiagnoses} />
            <TopMedicationsChartPanel items={topMedications} />
            <AIStatusChartPanel trend={aiTrend} />
          </section>
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
