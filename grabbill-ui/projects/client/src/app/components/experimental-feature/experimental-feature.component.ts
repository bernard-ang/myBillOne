import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'grabbill-client-experimental-feature',
  templateUrl: './experimental-feature.component.html',
  styleUrls: ['./experimental-feature.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExperimentalFeatureComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
