import { environment } from "../environments/environment";

export const ON_PREMISE_MODE = "on-premise";
export const SAAS_MODE = "saas";

export const isOnPremiseMode = () => {
  return environment.config.deploymentMode === ON_PREMISE_MODE;
};

export const isSaasMode = () => {
  return environment.config.deploymentMode === SAAS_MODE;
};
