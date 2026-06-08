import { Sparkles } from 'lucide-react';

interface DocumentAnalysisActionProps {
  disabled: boolean;
  onRequest: () => Promise<void>;
}

export function DocumentAnalysisAction({ disabled, onRequest }: DocumentAnalysisActionProps) {
  return (
    <button className="document-action document-action--analysis" type="button" disabled={disabled} onClick={() => void onRequest()}>
      <Sparkles aria-hidden="true" size={17} />
      Analizar IA
    </button>
  );
}
