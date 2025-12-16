import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Params } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { NzMessageService } from 'ng-zorro-antd/message';
import { Select, Store } from '@ngxs/store';
import { getJobStatusTag, JobModel } from '@grabbill/lib';
import { environment } from '../../../../environments/environment';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { GetJob, ResetJob } from '../../../../states/job-management/job-management.state-actions';
import { JobManagementState } from '../../../../states/job-management/job-management.state';

interface RecordData {
  no: number;
  email: string;
  attachmentFileName: string;
  attachmentPassword: string;
  mobileNo: string;
}

@Component({
  selector: 'grabbill-admin-job-detail',
  templateUrl: './job-detail.component.html',
  styleUrls: ['./job-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobDetailComponent extends NgxsBaseComponent {
  @Select(JobManagementState.job)
  job$!: Observable<JobModel>;

  id?: number;

  constructor(
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();
    this.store.dispatch(new ResetJob());

    this.autoUnsubscribe(
      this.route.params.pipe(
        tap((params: Params) => {
          this.id = params['id'];
          this.store.dispatch(new GetJob(this.id!));
        })
      )
    );
  }

  getStatusTag(job: JobModel): string {
    return getJobStatusTag(job.status);
  }

  getDateTimeFormat() {
    return environment.config.dateTimeFormat;
  }
}
