import { useEffect, useMemo, useState } from 'react';
import { getAIAnalysis, listDocumentAnalyses, requestDocumentAnalysis, retryAIAnalysis } from './documentsApi';
import { DocumentAnalysisAction } from './DocumentAnalysisAction';
import { DocumentAnalysisResult } from './DocumentAnalysisResult';
import { DocumentAnalysisRetryButton } from './DocumentAnalysisRetryButton';
import { DocumentAnalysisStatusBadge } from './DocumentAnalysisStatusBadge';
import type { AIAnalysis, AIAnalysisDocumentStatus, AIAnalysisSummary } from './documents.types';

interface DocumentAnalysisPanelProps {
  documentId: string;
  initialStatus: AIAnalysisDocumentStatus;
  onStatusChange: () => Promise<void>;
}

function normalizeStatus(status?: string): AIAnalysisDocumentStatus {
  if (
    status === 'pending' ||
    status === 'processing' ||
    status === 'completed' ||
    status === 'failed' ||
    status === 'reviewed' ||
    status === 'rejected' ||
    status === 'deleted' ||
    status === 'not_requested'
  ) {
    return status;
  }
  return 'not_requested';
}

export function DocumentAnalysisPanel({ documentId, initialStatus, onStatusChange }: DocumentAnalysisPanelProps) {
  const [analyses, setAnalyses] = useState<AIAnalysisSummary[]>([]);
  const [selectedAnalysis, setSelectedAnalysis] = useState<AIAnalysis | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const latestAnalysis = useMemo(() => analyses[0], [analyses]);
  const status = normalizeStatus(latestAnalysis?.status ?? initialStatus);
  const isInProgress = status === 'pending' || status === 'processing';

  async function loadAnalyses() {
    setError(null);
    try {
      const response = await listDocumentAnalyses(documentId);
      setAnalyses(response);
      const latest = response[0];
      setSelectedAnalysis(latest?.status === 'completed' ? await getAIAnalysis(latest.id) : null);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar análisis IA');
    }
  }

  useEffect(() => {
    void loadAnalyses();
  }, [documentId]);

  async function handleRequest() {
    setIsLoading(true);
    setError(null);
    try {
      const response = await requestDocumentAnalysis(documentId);
      setSelectedAnalysis(response.status === 'completed' ? response : null);
      await loadAnalyses();
      await onStatusChange();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible solicitar análisis IA');
    } finally {
      setIsLoading(false);
    }
  }

  async function handleRetry() {
    if (!latestAnalysis) {
      return;
    }

    setIsLoading(true);
    setError(null);
    try {
      const response = await retryAIAnalysis(latestAnalysis.id);
      setSelectedAnalysis(response.status === 'completed' ? response : null);
      await loadAnalyses();
      await onStatusChange();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible reintentar análisis IA');
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section className="document-analysis-panel" aria-label="Análisis documental IA">
      <div className="document-analysis-panel__header">
        <DocumentAnalysisStatusBadge status={status} />
        <div className="document-analysis-panel__actions">
          <DocumentAnalysisAction disabled={isLoading || isInProgress} onRequest={handleRequest} />
          {latestAnalysis?.status === 'failed' ? <DocumentAnalysisRetryButton disabled={isLoading} onRetry={handleRetry} /> : null}
        </div>
      </div>

      {error ? <div className="document-analysis-panel__error" role="alert">{error}</div> : null}
      {latestAnalysis?.status === 'failed' ? <p className="document-analysis-panel__note">{latestAnalysis.errorMessage ?? 'El análisis falló.'}</p> : null}
      {selectedAnalysis ? <DocumentAnalysisResult analysis={selectedAnalysis} /> : null}
    </section>
  );
}
