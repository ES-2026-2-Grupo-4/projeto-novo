import { CheckCircleIcon } from './Icons.tsx';

export interface StepItem {
  number: number;
  title: string;
  shortTitle: string;
}

interface StepProgressProps {
  currentStep: number;
  steps: StepItem[];
  onStepClick?: (stepNumber: number) => void;
}

export function StepProgress({ currentStep, steps, onStepClick }: StepProgressProps) {
  return (
    <div className="stepper-wrapper" aria-label="Progresso do cadastro">
      <div className="stepper-list">
        {steps.map((step, index) => {
          const isCompleted = currentStep > step.number;
          const isActive = currentStep === step.number;
          const isClickable = onStepClick && step.number < currentStep;

          return (
            <div
              key={step.number}
              className={`stepper-item ${isCompleted ? 'step-completed' : ''} ${
                isActive ? 'step-active' : ''
              }`}
            >
              <div
                className={`stepper-bubble ${isClickable ? 'clickable' : ''}`}
                onClick={() => isClickable && onStepClick(step.number)}
                role={isClickable ? 'button' : undefined}
                tabIndex={isClickable ? 0 : -1}
                title={isClickable ? `Voltar para ${step.title}` : undefined}
              >
                {isCompleted ? (
                  <CheckCircleIcon className="stepper-check-icon" />
                ) : (
                  <span>{step.number}</span>
                )}
              </div>

              <div className="stepper-label-box">
                <span className="stepper-step-number">Etapa {step.number}</span>
                <span className="stepper-step-title">{step.title}</span>
              </div>

              {index < steps.length - 1 && (
                <div
                  className={`stepper-connector ${
                    currentStep > step.number ? 'connector-active' : ''
                  }`}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
