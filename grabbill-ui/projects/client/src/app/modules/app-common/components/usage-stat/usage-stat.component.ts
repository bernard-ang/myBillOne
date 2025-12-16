import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import prettyBytes from 'pretty-bytes';
import { DashboardStatisticsModel, Privilege } from '@grabbill/lib';
import { environment } from '../../../../../environments/environment';
import { isExperimental } from '../../../../../utils/has-privilege';
import { isOnPremiseMode, isSaasMode } from "../../../../../utils/deployment-mode";

@Component({
  selector: 'grabbill-client-usage-stat',
  templateUrl: './usage-stat.component.html',
  styleUrls: ['./usage-stat.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsageStatComponent implements OnInit, OnChanges {
  @Input()
  title: string | undefined;

  @Input()
  currentStatistics!: DashboardStatisticsModel;

  storagePercentage = 0;
  transactionalEmailPercentage = 0;
  marketingEmailPercentage = 0;
  smsCreditPercentage = 0;

  shortDateFormat = environment.config.shortDateFormat;
  isSaasMode = isSaasMode();
  isOnPremiseMode = isOnPremiseMode();

  constructor(private cd: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.updatePercentage();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.updatePercentage();
  }

  private updatePercentage() {
    if (this.currentStatistics) {
      this.storagePercentage = Math.floor(
        (this.currentStatistics.storageUsed / this.currentStatistics.storageLimit) * 100
      );
      this.transactionalEmailPercentage = Math.floor(
        (this.currentStatistics.transactionalEmailSent / this.currentStatistics.transactionalEmailLimit) * 100
      );
      this.marketingEmailPercentage = Math.floor(
        (this.currentStatistics.emailCampaignSent / this.currentStatistics.emailCampaignLimit) * 100
      );

      const total = this.currentStatistics.smsRemainingCredits + this.currentStatistics.smsTotalCreditsUsed;
      if (total !== 0) {
        this.smsCreditPercentage = Math.floor((this.currentStatistics.smsTotalCreditsUsed / total) * 100);
      }
      this.cd.markForCheck();
    }
  }

  isExperimentalFeature() {
    return isExperimental();
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }
}
