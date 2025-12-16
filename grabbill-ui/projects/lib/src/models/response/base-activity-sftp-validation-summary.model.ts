export interface BaseActivitySftpValidationSummaryModel {
  hasError: boolean;

  genericErrors: string[];

  indexRowErrors: Record<string, IndexRowError[]>;

  fileErrors: Record<string, string>;
}

export interface IndexRowError {
  columnNo: number;
  error: string;
}
