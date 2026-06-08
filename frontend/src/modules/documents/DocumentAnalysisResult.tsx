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
      ) : <p>No registrado en el análisis simulado.</p>}
    </div>
  );
}

export function DocumentAnalysisResult({ analysis }: DocumentAnalysisResultProps) {
  return (
    <section className="document-analysis-result" aria-label="Resultado de análisis IA">
      <div className="document-analysis-result__warning">
        <AlertTriangle aria-hidden="true" size={16} />
        {analysis.disclaimer || AI_DISCLAIMER}
      </div>

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
