import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';
import { ActivatedRoute, Params } from '@angular/router';
import { ActionCompletion, Actions, ofActionCompleted, Select, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { Observable, of, switchMap, tap } from 'rxjs';
import { GetJob, ResetJob } from '../../../../states/job/job.state-actions';
import { ShowMessage } from '../../../../states/common/common.state-actions';
import { getErrorMessage, getJobStatusTag, JobModel } from "@grabbill/lib";
import { JobState } from '../../../../states/job/job.state';

@Component({
  selector: 'grabbill-client-job-detail',
  templateUrl: './job-detail.component.html',
  styleUrls: ['./job-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class JobDetailComponent extends NgxsBaseComponent {
  @Select(JobState.job)
  job$!: Observable<JobModel>;

  jobId?: number;

  constructor(
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private actions$: Actions,
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
          this.jobId = params['id'];
          this.store.dispatch(new GetJob(this.jobId!));
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(GetJob),
        switchMap((data: ActionCompletion) => {
          if (data.result.error) {
            this.store.dispatch(new ShowMessage('error', getErrorMessage(data.result.error)));
          }
          return of(false);
        })
      )
    );
  }

  getStatusTag(job: JobModel): string {
    return getJobStatusTag(job.status);
  }
}
