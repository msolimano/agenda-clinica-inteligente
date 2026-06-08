import { CheckCircle2, Ban } from 'lucide-react';

interface AIConsentActionProps {
  active: boolean;
  disabled: boolean;
  onAccept: () => Promise<void>;
  onRevoke: () => Promise<void>;
}

export function AIConsentAction({ active, disabled, onAccept, onRevoke }: AIConsentActionProps) {
  if (active) {
    return (
      <button className="document-action document-action--consent-revoke" type="button" disabled={disabled} onClick={() => void onRevoke()}>
        <Ban aria-hidden="true" size={17} />
        Revocar
      </button>
    );
  }

  return (
    <button className="document-action document-action--consent-accept" type="button" disabled={disabled} onClick={() => void onAccept()}>
      <CheckCircle2 aria-hidden="true" size={17} />
      Aceptar IA
    </button>
  );
}
