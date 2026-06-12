import type { ClinicalInsightType } from './clinicalInsights.types';

const labels: Record<ClinicalInsightType, string> = {
  clinical_summary: 'Resumen clinico',
  clinical_alert: 'Alerta clinica',
  diagnosis_candidate: 'Diagnostico sugerido',
  medication_candidate: 'Medicamento mencionado',
  allergy_candidate: 'Alergia mencionada',
  risk_factor_candidate: 'Factor de riesgo',
  lab_result_candidate: 'Hallazgo laboratorio',
  observation_candidate: 'Observacion sugerida'
};

interface ClinicalInsightTypeBadgeProps {
  type: ClinicalInsightType;
}

export function ClinicalInsightTypeBadge({ type }: ClinicalInsightTypeBadgeProps) {
  return <span className={`clinical-insight-type clinical-insight-type--${type}`}>{labels[type]}</span>;
}
