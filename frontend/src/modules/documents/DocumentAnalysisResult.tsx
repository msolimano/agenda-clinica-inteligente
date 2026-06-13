import { AlertTriangle } from 'lucide-react';
import type { AIAnalysis } from './documents.types';

const AI_DISCLAIMER = 'Resultado generado por IA. No constituye diagnóstico médico y debe ser revisado por un profesional de salud.';

interface DocumentAnalysisResultProps {
  analysis: AIAnalysis;
}

function BulletList({ title, items }: { title: string; items: string[] }) {
  return (
    <div className="document-analysis-result__group">
      <h4>{title}</h4>
      {items.length ? (
        <ul>
          {items.map((item) => <li key={item}>{item}</li>)}
        </ul>
      ) : <p>No registrado en el análisis IA.</p>}
    </div>
  );
}

function extractionLabel(status: string | null) {
  if (status === 'completed') return 'Texto extraído correctamente';
  if (status === 'unsupported') return 'Documento no soportado para OCR';
  if (status === 'empty') return 'Sin texto detectable';
  if (status === 'failed') return 'Error de OCR';
  return 'No solicitado';
}

export function DocumentAnalysisResult({ analysis }: DocumentAnalysisResultProps) {
  const providerLabel = analysis.providerName === 'openai' ? 'OpenAI' : analysis.providerName === 'mock' ? 'Mock' : 'No informado';
  const modelLabel = analysis.modelName ?? 'No informado';
  const promptLabel = analysis.promptVersion ?? 'No informado';
  const latencyLabel = analysis.latencyMs == null ? 'No informado' : `${analysis.latencyMs} ms`;
  const extractionConfidence = analysis.textExtractionConfidence == null ? null : `${Math.round(analysis.textExtractionConfidence * 100)}%`;

  return (
    <section className="document-analysis-result" aria-label="Resultado de análisis IA">
      <div className="document-analysis-result__warning">
        <AlertTriangle aria-hidden="true" size={16} />
        {analysis.disclaimer || AI_DISCLAIMER}
      </div>

      <div className="document-analysis-result__meta">
        <span>Proveedor: {providerLabel}</span>
        <span>Modelo: {modelLabel}</span>
        <span>Prompt: {promptLabel}</span>
        <span>Latencia: {latencyLabel}</span>
        <span>OCR: {extractionLabel(analysis.textExtractionStatus)}{extractionConfidence ? ` (${extractionConfidence})` : ''}</span>
      </div>

      {analysis.textExtractionErrorMessage ? (
        <div className="document-analysis-result__group">
          <h4>Extracción de texto</h4>
          <p>{analysis.textExtractionErrorMessage}</p>
        </div>
      ) : null}

      {analysis.extractedTextPreview ? (
        <div className="document-analysis-result__group">
          <h4>Texto extraído preview</h4>
          <p>{analysis.extractedTextPreview}</p>
        </div>
      ) : null}

      <div className="document-analysis-result__group">
        <h4>Resumen clínico</h4>
        <p>{analysis.clinicalSummary ?? 'Sin resumen disponible.'}</p>
      </div>

      <BulletList title="Hallazgos relevantes" items={analysis.relevantFindings} />
      <BulletList title="Diagnósticos mencionados" items={analysis.mentionedDiagnoses} />
      <BulletList title="Medicamentos mencionados" items={analysis.mentionedMedications} />
      <BulletList title="Alergias mencionadas" items={analysis.mentionedAllergies} />

      <div className="document-analysis-result__group">
        <h4>Recomendaciones u observaciones</h4>
        <p>{analysis.recommendations ?? 'Sin observaciones disponibles.'}</p>
      </div>
    </section>
  );
}
