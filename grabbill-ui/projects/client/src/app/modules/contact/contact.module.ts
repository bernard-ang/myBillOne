import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContactListComponent } from './contact-list/contact-list.component';
import { RouterModule } from '@angular/router';
import { ContactGroupListComponent } from './contact-group-list/contact-group-list.component';
import { ContactFieldListComponent } from './contact-field-list/contact-field-list.component';
import { NzTypographyModule } from 'ng-zorro-antd/typography';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzFormModule } from 'ng-zorro-antd/form';
import { ReactiveFormsModule } from '@angular/forms';
import { NzListModule } from 'ng-zorro-antd/list';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { ContactDetailComponent } from './contact-detail/contact-detail.component';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzTimelineModule } from 'ng-zorro-antd/timeline';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzDescriptionsModule } from 'ng-zorro-antd/descriptions';
import { PrivilegeGuard } from '../../guards/privilege.guard';
import { Privilege } from '@grabbill/lib';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { ContactGroupApi } from '../../../api/contact-group.api';
import { ContactGroupService } from '../../../services/contact-group.service';
import { NgxsModule } from '@ngxs/store';
import { ContactGroupState } from '../../../states/contact-group/contact-group.state';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { ContactFieldApi } from '../../../api/contact-field.api';
import { ContactFieldService } from '../../../services/contact-field.service';
import { ContactFieldState } from '../../../states/contact-field/contact-field.state';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzInputNumberModule } from 'ng-zorro-antd/input-number';
import { ContactApi } from '../../../api/contact.api';
import { ContactService } from '../../../services/contact.service';
import { ContactState } from '../../../states/contact/contact.state';
import { AppCommonModule } from '../app-common/app-common.module';
import { NzSkeletonModule } from 'ng-zorro-antd/skeleton';
import { ModalContactGroupComponent } from './modal-contact-group/modal-contact-group.component';
import { NzUploadModule } from 'ng-zorro-antd/upload';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { ContactRowErrorModalComponent } from './contact-row-error-modal/contact-row-error-modal.component';
import { ContactsDetailComponent } from './contacts-detail/contacts-detail.component';
import { NzTabsModule } from "ng-zorro-antd/tabs";

@NgModule({
  declarations: [
    ContactListComponent,
    ContactGroupListComponent,
    ContactFieldListComponent,
    ContactDetailComponent,
    ModalContactGroupComponent,
    ContactRowErrorModalComponent,
    ContactsDetailComponent,
  ],
  imports: [
    CommonModule,
    RouterModule.forChild([
      {
        path: 'list',
        component: ContactsDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.CONTACT_VIEW },
      },
      {
        path: 'detail/:id',
        component: ContactDetailComponent,
        canActivate: [PrivilegeGuard],
        data: { privilege: Privilege.CONTACT_VIEW },
      },
    ]),
    ReactiveFormsModule,
    NgxsModule.forFeature([ContactGroupState, ContactFieldState, ContactState]),
    AppCommonModule,
    NzTypographyModule,
    NzButtonModule,
    NzSelectModule,
    NzCheckboxModule,
    NzInputModule,
    NzIconModule,
    NzTableModule,
    NzFormModule,
    NzListModule,
    NzDividerModule,
    NzTagModule,
    NzBreadCrumbModule,
    NzAvatarModule,
    NzTimelineModule,
    NzCardModule,
    NzDescriptionsModule,
    NzEmptyModule,
    NzDrawerModule,
    NzModalModule,
    NzDatePickerModule,
    NzInputNumberModule,
    NzSkeletonModule,
    NzUploadModule,
    NzToolTipModule,
    NzTabsModule,
  ],
  providers: [
    { provide: ContactGroupApi, useClass: ContactGroupService },
    { provide: ContactFieldApi, useClass: ContactFieldService },
    { provide: ContactApi, useClass: ContactService },
  ],
})
export class ContactModule {}
