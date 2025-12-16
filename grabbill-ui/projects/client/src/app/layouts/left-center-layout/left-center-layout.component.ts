import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'grabbill-client-left-center-layout',
  templateUrl: './left-center-layout.component.html',
  styleUrls: ['./left-center-layout.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LeftCenterLayoutComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
