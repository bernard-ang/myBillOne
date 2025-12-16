import { NgModule } from '@angular/core';
import { NZ_ICONS, NzIconModule } from 'ng-zorro-antd/icon';

import { CheckOutline, CloseOutline, MenuOutline } from '@ant-design/icons-angular/icons';

const icons = [CheckOutline, CloseOutline, MenuOutline];

@NgModule({
  imports: [NzIconModule],
  exports: [NzIconModule],
  providers: [{ provide: NZ_ICONS, useValue: icons }],
})
export class IconsProviderModule {}
