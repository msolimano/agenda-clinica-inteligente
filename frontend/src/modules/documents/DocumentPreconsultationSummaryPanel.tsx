import { AlertTriangle, ClipboardList, Pill, ShieldAlert, Stethoscope, TestTube2 } from 'lucide-react';
import { useEffect, useMemo, useState, type ReactNode } from 'react';
import { listDocumentClinicalInsights } from './clinicalInsightsApi';
import type { ClinicalInsight } from './clinicalInsights.types';
import type { AIAnalysis } from './documents.types';

const DISCLAIMER = 'Sugerencia generada por IA. Debe ser revisada por un profesional de salud. No constituye diagnóstico médico.';

interface DocumentPreconsultationSummaryPanelProps {
  analysis: AIAnalysis;
  documentId: string;
  refreshKey: number;
}

function unique(values: Array<string | null | undefined>) {
  return Array.from(new Set(values.map((value) => value?.trim()).filter((value): value is string => Boolean(value))));
}

function normalizeInsightItems(insights: ClinicalInsight[], types: string[]) {
  return insights
    .filter((insight) => types.includes(insight.insightType))
    .map((insight) => insight.description || insight.title)
    .filter(Boolean);
}

function confidenceSummary(insights: ClinicalInsight[]) {
  const values = insights
    .map((insight) => insight.confidence)
    .filter((value): value is number => typeof value === 'number');

  if (!values.length) {
    return { label: 'No informada', detail: 'La IA no entrego confianza numerica para este resumen.' };
  }

  const average = values.reduce((total, value) => total + value, 0) / values.length;
  const percentage = Math.round(average * 100);
  const label = percentage >= 75 ? 'Alta' : percentage >= 50 ? 'Media' : 'Baja';
  return { label, detail: `${percentage}% promedio sobre ${values.length} insight${values.length === 1 ? '' : 's'}.` };
}

function SummarySection({ title, icon, items, emptyText }: { title: string; icon: ReactNode; items: string[]; emptyText: string }) {
  return (
    <section className="preconsultation-summary__section">
      <div className="preconsultation-summary__section-title">
        {icon}
        <h4>{title}</h4>
      </div>
      {items.length ? (
        <ul>
          {items.map((item) => <li key={item}>{item}</li>)}
        </ul>
      ) : <p>{emptyText}</p>}
    </section>
  );
}

export function DocumentPreconsultationSummaryPanel({ analysis, documentId, refreshKey }: DocumentPreconsultationSummaryPanelProps) {
  const [insights, setInsights] = useState<ClinicalInsight[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadInsights() {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listDocumentClinicalInsights(documentId);
      setInsights(response);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar el resumen de preconsulta');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadInsights();
  }, [documentId, refreshKey]);

  const grouped = useMemo(() => {
    const findings = unique([
      ...analysis.relevantFindings,
      ...normalizeInsightItems(insights, ['observation_candidate', 'lab_result_candidate'])
    ]);
    const medications = unique([
      ...analysis.mentionedMedications,
      ...normalizeInsightItems(insights, ['medication_candidate'])
    ]);
    const allergies = unique([
      ...analysis.mentionedAllergies,
      ...normalizeInsightItems(insights, ['allergy_candidate'])
    ]);
    const risks = unique(normalizeInsightItems(insights, ['risk_factor_candidate']));
    const alerts = unique(normalizeInsightItems(insights, ['clinical_alert']));
    const recommendations = unique([analysis.recommendations, ...normalizeInsightItems(insights, ['clinical_summary'])]);
    return { findings, medications, allergies, risks, alerts, recommendations };
  }, [analysis, insights]);

  const confidence = useMemo(() => confidenceSummary(insights), [insights]);

  return (
    <section className="preconsultation-summary" aria-label="Resumen IA de Preconsulta">
      <div className="preconsultation-summary__header">
        <span className="preconsultation-summary__icon"><Stethoscope aria-hidden="true" size={20} /></span>
        <div>
          <p className="preconsultation-summary__eyebrow">Resumen IA de Preconsulta</p>
          <h3 className="preconsultation-summary__title">Contexto clínico previo a la atención</h3>
          <p className="preconsultation-summary__subtitle">Síntesis orientativa basada en el análisis IA documental y los insights pendientes de revisión.</p>
        </div>
      </div>

      <div className="preconsultation-summary__disclaimer">
        <AlertTriangle aria-hidden="true" size={16} />
        {analysis.disclaimer || DISCLAIMER}
      </div>

      {error ? <div className="preconsultation-summary__error" role="alert">{error}</div> : null}
      {isLoading ? <p className="preconsultation-summary__loading">Cargando resumen de preconsulta...</p> : null}

      <section className="preconsultation-summary__executive">
        <div>
          <h4>Resumen ejecutivo</h4>
          <p>{analysis.clinicalSummary ?? 'Sin resumen clínico disponible para este documento.'}</p>
        </div>
        <div className={`preconsultation-summary__confidence preconsultation-summary__confidence--${confidence.label.toLowerCase().replace(' ', '-')}`}>
          <span>Nivel de confianza</span>
          <strong>{confidence.label}</strong>
          <small>{confidence.detail}</small>
        </div>
      </section>

      <div className="preconsultation-summary__grid">
        <SummarySection
          title="Hallazgos relevantes"
          icon={<ClipboardList aria-hidden="true" size={17} />}
          items={grouped.findings}
          emptyText="No se detectaron hallazgos relevantes estructurados."
        />
        <SummarySection
          title="Medicamentos detectados"
          icon={<Pill aria-hidden="true" size={17} />}
          items={grouped.medications}
          emptyText="No se detectaron medicamentos mencionados."
        />
        <SummarySection
          title="Alergias"
          icon={<ShieldAlert aria-hidden="true" size={17} />}
          items={grouped.allergies}
          emptyText="No se detectaron alergias mencionadas."
        />
        <SummarySection
          title="Riesgos identificados"
          icon={<AlertTriangle aria-hidden="true" size={17} />}
          items={grouped.risks}
          emptyText="No se identificaron factores de riesgo estructurados."
        />
        <SummarySection
          title="Alertas importantes"
          icon={<ShieldAlert aria-hidden="true" size={17} />}
          items={grouped.alerts}
          emptyText="No se generaron alertas clínicas prioritarias."
        />
        <SummarySection
          title="Recomendaciones de revisión médica"
          icon={<TestTube2 aria-hidden="true" size={17} />}
          items={grouped.recommendations}
          emptyText="Revisar el documento original y contrastar con la evaluación clínica."
        />
      </div>
    </section>
  );
}
