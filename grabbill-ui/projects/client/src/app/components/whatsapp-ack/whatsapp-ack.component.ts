import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { Actions, Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { tap } from 'rxjs';
import { NgxsBaseComponent } from '../ngxs-base.component';
import { ActivatedRoute } from '@angular/router';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'grabbill-client-whatsapp-ack',
  templateUrl: './whatsapp-ack.component.html',
  styleUrls: ['./whatsapp-ack.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WhatsappAckComponent extends NgxsBaseComponent {
  status = '';

  get environment() {
    return environment.config;
  }

  constructor(
    private route: ActivatedRoute,
    protected override store: Store,
    protected override messageService: NzMessageService
  ) {
    super(store, messageService);
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.autoUnsubscribe(
      this.route.queryParams.pipe(
        tap((params) => {
          this.status = params['status'];
        })
      )
    );
  }
}
