import { RotateCcw } from 'lucide-react';

interface DocumentAnalysisRetryButtonProps {
  disabled: boolean;
  onRetry: () => Promise<void>;
}

export function DocumentAnalysisRetryButton({ disabled, onRetry }: DocumentAnalysisRetryButtonProps) {
  return (
    <button className="document-action document-action--retry" type="button" disabled={disabled} onClick={() => void onRetry()}>
      <RotateCcw aria-hidden="true" size={17} />
      Reintentar
    </button>
  );
}
