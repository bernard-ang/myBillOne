import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';

@Component({
  selector: 'grabbill-admin-left-center-layout',
  templateUrl: './admin-left-center-layout.component.html',
  styleUrls: ['./admin-left-center-layout.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminLeftCenterLayoutComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {}
}
