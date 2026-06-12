import { Lightbulb } from 'lucide-react';
import { useEffect, useState } from 'react';
import { acceptClinicalInsight, dismissClinicalInsight, listDocumentClinicalInsights, rejectClinicalInsight } from './clinicalInsightsApi';
import { ClinicalInsightCard } from './ClinicalInsightCard';
import type { ClinicalInsight } from './clinicalInsights.types';

interface DocumentClinicalInsightsPanelProps {
  documentId: string;
  refreshKey: number;
}

export function DocumentClinicalInsightsPanel({ documentId, refreshKey }: DocumentClinicalInsightsPanelProps) {
  const [insights, setInsights] = useState<ClinicalInsight[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [savingInsightId, setSavingInsightId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function loadInsights() {
    if (!documentId) {
      setInsights([]);
      setError(null);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const response = await listDocumentClinicalInsights(documentId);
      setInsights(response);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar insights clinicos');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadInsights();
  }, [documentId, refreshKey]);

  async function reviewInsight(insight: ClinicalInsight, action: 'accept' | 'reject' | 'dismiss') {
    setSavingInsightId(insight.id);
    setError(null);
    try {
      const payload = { reviewNotes: action === 'accept' ? 'Insight aceptado desde modulo Documentos' : action === 'reject' ? 'Insight rechazado desde modulo Documentos' : 'Insight descartado desde modulo Documentos' };
      if (action === 'accept') {
        await acceptClinicalInsight(insight.id, payload);
      } else if (action === 'reject') {
        await rejectClinicalInsight(insight.id, payload);
      } else {
        await dismissClinicalInsight(insight.id, payload);
      }
      await loadInsights();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar el insight clinico');
    } finally {
      setSavingInsightId(null);
    }
  }

  return (
    <section className="document-clinical-insights" aria-label="Insights clinicos del documento">
      <div className="document-clinical-insights__header">
        <span className="document-clinical-insights__icon"><Lightbulb aria-hidden="true" size={18} /></span>
        <div>
          <h3 className="document-clinical-insights__title">Clinical Insight Engine</h3>
          <p className="document-clinical-insights__subtitle">Sugerencias estructuradas generadas desde el analisis IA documental.</p>
        </div>
      </div>

      <p className="document-clinical-insights__disclaimer">Sugerencia generada por IA. Debe ser revisada por un profesional de salud. No constituye diagnóstico médico.</p>
      {isLoading ? <p className="document-clinical-insights__empty">Cargando insights clinicos...</p> : null}
      {error ? <div className="document-clinical-insights__error" role="alert">{error}</div> : null}
      {!isLoading && !error && insights.length === 0 ? <p className="document-clinical-insights__empty">Sin insights clínicos generados.</p> : null}

      {insights.length ? (
        <div className="document-clinical-insights__list">
          {insights.map((insight) => (
            <ClinicalInsightCard
              key={insight.id}
              insight={insight}
              isSaving={savingInsightId === insight.id}
              onAccept={(item) => reviewInsight(item, 'accept')}
              onReject={(item) => reviewInsight(item, 'reject')}
              onDismiss={(item) => reviewInsight(item, 'dismiss')}
            />
          ))}
        </div>
      ) : null}
    </section>
  );
}
