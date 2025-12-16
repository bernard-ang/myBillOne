import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Store } from '@ngxs/store';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NgxsBaseComponent } from '../../../components/ngxs-base.component';

@Component({
  selector: 'grabbill-client-contacts-detail',
  templateUrl: './contacts-detail.component.html',
  styleUrls: ['./contacts-detail.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactsDetailComponent extends NgxsBaseComponent {
  constructor(public override store: Store, public override messageService: NzMessageService) {
    super(store, messageService);
  }
}
