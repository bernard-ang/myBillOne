import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { Select, Store } from '@ngxs/store';
import { Observable, tap } from 'rxjs';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { AdminDashboardStatisticsModel, KeyToCount } from '@grabbill/lib';
import { DashboardManagementState } from '../../../states/dashboard-management/dashboard-management.state';
import { NzMessageService } from 'ng-zorro-antd/message';
import { GetCurrentStatistics } from '../../../states/dashboard-management/dashboard-management.state-actions';
import { CountriesData } from 'countries-map';
import prettyBytes from 'pretty-bytes';

@Component({
  selector: 'grabbill-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminDashboardComponent extends NgxsBaseComponent {
  @Select(DashboardManagementState.currentStatistics)
  currentStatistics$!: Observable<AdminDashboardStatisticsModel>;

  mapData: CountriesData = {};

  constructor(
    private cd: ChangeDetectorRef,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.currentStatistics$.pipe(
        tap((currentStatistics) => {
          if (currentStatistics) {
            currentStatistics.accountsByCountry.map((item) => {
              this.mapData[item.key] = { value: item.count };
            });
            this.cd.markForCheck();
          }
        })
      )
    );

    this.store.dispatch(new GetCurrentStatistics());
  }

  getKeyCount(targetKey: string, keyToCounts: KeyToCount[]): number {
    const filteredKeyToCount = keyToCounts.filter((item) => item.key === targetKey);

    return filteredKeyToCount.length > 0 ? filteredKeyToCount[0].count : 0;
  }

  formatBytes(bytes: number) {
    return prettyBytes(bytes, { locale: true });
  }
}
