import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core';
import { Privilege, UserAuthorityModel } from '@grabbill/lib';
import { Store } from '@ngxs/store';
import { hasPrivilege } from '../../../../../utils/has-privilege';
import { AuthState } from '../../../../../states/auth/auth.state';

interface MyContext {
  $implicit: UserAuthorityModel;
}

@Component({
  selector: 'grabbill-client-page-header',
  templateUrl: './page-header.component.html',
  styleUrls: ['./page-header.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageHeaderComponent implements OnInit {
  @Input()
  title!: string;

  @Input()
  subtitle?: string;

  @Input()
  description?: string;

  @Input()
  margin = false;

  @Input()
  button?: { label: string; link?: string[]; privilege: Privilege; click?: () => void };

  @Input()
  buttonTemplate?: TemplateRef<MyContext>;

  @Input()
  extraTemplate?: TemplateRef<MyContext>;

  @Output()
  buttonClick = new EventEmitter<void>();

  user?: UserAuthorityModel;

  constructor(private store: Store) {}

  ngOnInit(): void {
    this.user = this.store.selectSnapshot(AuthState.user)!;
  }

  hasPrivilege(privilege: Privilege) {
    return hasPrivilege(this.user!, privilege);
  }

  handleButtonClick() {
    this.buttonClick.emit();
  }
}
