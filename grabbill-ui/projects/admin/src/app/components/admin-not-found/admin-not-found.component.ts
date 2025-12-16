import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';

@Component({
  selector: 'grabbill-admin-not-found',
  templateUrl: './admin-not-found.component.html',
  styleUrls: ['./admin-not-found.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminNotFoundComponent implements OnInit {
  constructor() {}

  ngOnInit(): void {}
}
