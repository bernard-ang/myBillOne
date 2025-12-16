export const defaultMjmlTemplate = (appUrl: string) =>
  '<mjml>\n' +
  '  <mj-body>\n' +
  '    <mj-section>\n' +
  '      <mj-column>\n' +
  `        <mj-image src="${appUrl}/assets/logo_big.png" />\n` +
  '        <mj-text font-size="18px" align="center" __p="false" font-weight="700">Your Tagline\n' +
  '        </mj-text>\n' +
  '      </mj-column>\n' +
  '    </mj-section>\n' +
  '    <mj-section>\n' +
  '      <mj-column>\n' +
  '        <mj-text font-size="15px" __p="false" font-weight="700">Section Heading\n' +
  '        </mj-text>\n' +
  '        <mj-text>\n' +
  '          <span id="ie176">Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</span>\n' +
  '        </mj-text>\n' +
  '        <mj-button href="https://www.mybillone.com">Button\n' +
  '        </mj-button>\n' +
  '      </mj-column>\n' +
  '    </mj-section>\n' +
  '    <mj-section>\n' +
  '      <mj-column>\n' +
  '        <mj-text align="center">\n' +
  '          <a href="{{unsubscribe_link}}" title="Unsubscribe Link" target="_blank">Unsubscribe</a>\n' +
  '        </mj-text>\n' +
  '      </mj-column>\n' +
  '    </mj-section>\n' +
  '  </mj-body>\n' +
  '</mjml>';
