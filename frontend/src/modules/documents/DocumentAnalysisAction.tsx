import { Sparkles } from 'lucide-react';

interface DocumentAnalysisActionProps {
  disabled: boolean;
  title?: string;
  onRequest: () => Promise<void>;
}

export function DocumentAnalysisAction({ disabled, title, onRequest }: DocumentAnalysisActionProps) {
  return (
    <button className="document-action document-action--analysis" type="button" disabled={disabled} title={title} onClick={() => void onRequest()}>
      <Sparkles aria-hidden="true" size={17} />
      Analizar IA
    </button>
  );
}
