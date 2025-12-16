import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'grabbill-client-center-layout',
  templateUrl: './center-layout.component.html',
  styleUrls: ['./center-layout.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CenterLayoutComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
