import type { BISummary } from './biDashboard.types';

interface SummaryKpiCardsProps {
  summary: BISummary;
}

export function SummaryKpiCards({ summary }: SummaryKpiCardsProps) {
  const cards = [
    { label: 'Citas operativas', value: summary.agenda.totalAppointments },
    { label: 'Pacientes activos', value: summary.patients.activePatients },
    { label: 'Fichas clinicas', value: summary.clinical.clinicalRecordsTotal },
    { label: 'Documentos activos', value: summary.documents.clinicalDocumentsTotal },
    { label: 'Analisis IA', value: summary.ai.aiAnalysesTotal },
    { label: 'Recursos FHIR', value: summary.fhir.fhirResourcesAvailable }
  ];

  return (
    <section className="bi-summary" aria-label="Resumen operacional">
      {cards.map((card) => (
        <article className="bi-kpi-card" key={card.label}>
          <span className="bi-kpi-card__label">{card.label}</span>
          <strong className="bi-kpi-card__value">{card.value}</strong>
        </article>
      ))}
    </section>
  );
}
