export const defaultHtmlTemplate = (appUrl: string) =>
  '<!doctype html>\n' +
  '<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">\n' +
  '  <head>\n' +
  '    <title>\n' +
  '    </title>\n' +
  '    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">\n' +
  '    <meta name="viewport" content="width=device-width, initial-scale=1">\n' +
  '    <style type="text/css">\n' +
  '      #outlook a {\n' +
  '        padding:0;\n' +
  '      }\n' +
  '      body {\n' +
  '        margin:0;\n' +
  '        padding:0;\n' +
  '        -webkit-text-size-adjust:100%;\n' +
  '        -ms-text-size-adjust:100%;\n' +
  '      }\n' +
  '      table, td {\n' +
  '        border-collapse:collapse;\n' +
  '        mso-table-lspace:0pt;\n' +
  '        mso-table-rspace:0pt;\n' +
  '      }\n' +
  '      img {\n' +
  '        border:0;\n' +
  '        height:auto;\n' +
  '        line-height:100%;\n' +
  '        outline:none;\n' +
  '        text-decoration:none;\n' +
  '        -ms-interpolation-mode:bicubic;\n' +
  '      }\n' +
  '      p {\n' +
  '        display:block;\n' +
  '        margin:13px 0;\n' +
  '      }\n' +
  '    </style>\n' +
  '    <style type="text/css">\n' +
  '      @media only screen and (min-width:480px) {\n' +
  '        .mj-column-per-100 {\n' +
  '          width:100% !important;\n' +
  '          max-width: 100%;\n' +
  '        }\n' +
  '      }\n' +
  '    </style>\n' +
  '    <style media="screen and (min-width:480px)">\n' +
  '      .moz-text-html .mj-column-per-100 {\n' +
  '        width:100% !important;\n' +
  '        max-width: 100%;\n' +
  '      }\n' +
  '    </style>\n' +
  '    <style type="text/css">\n' +
  '      @media only screen and (max-width:479px) {\n' +
  '        table.mj-full-width-mobile {\n' +
  '          width: 100% !important;\n' +
  '        }\n' +
  '        td.mj-full-width-mobile {\n' +
  '          width: auto !important;\n' +
  '        }\n' +
  '      }\n' +
  '    </style>\n' +
  '    <style type="text/css">\n' +
  '    </style>\n' +
  '  </head>\n' +
  '  <body style="word-spacing:normal;">\n' +
  '    <div\n' +
  '         style=""\n' +
  '         >\n' +
  '      <div  style="margin:0px auto;max-width:600px;">\n' +
  '        <table\n' +
  '               align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="width:100%;"\n' +
  '               >\n' +
  '          <tbody>\n' +
  '            <tr>\n' +
  '              <td\n' +
  '                  style="direction:ltr;font-size:0;padding:20px 0;text-align:center;"\n' +
  '                  >\n' +
  '                <div\n' +
  '                     class="mj-column-per-100 mj-outlook-group-fix" style="font-size:0;text-align:left;direction:ltr;display:inline-block;vertical-align:top;width:100%;"\n' +
  '                     >\n' +
  '                  <table\n' +
  '                         border="0" cellpadding="0" cellspacing="0" role="presentation" style="vertical-align:top;" width="100%"\n' +
  '                         >\n' +
  '                    <tbody>\n' +
  '                      <tr>\n' +
  '                        <td\n' +
  '                            align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;"\n' +
  '                            >\n' +
  '                          <table\n' +
  '                                 border="0" cellpadding="0" cellspacing="0" role="presentation" style="border-collapse:collapse;border-spacing:0px;"\n' +
  '                                 >\n' +
  '                            <tbody>\n' +
  '                              <tr>\n' +
  '                                <td  style="width:550px;">\n' +
  '                                  <img\n' +
  `                                       src="${appUrl}/assets/logo_big.png" style="border:0;display:block;outline:none;text-decoration:none;height:auto;width:100%;font-size:13px;" width="550" height="auto"\n` +
  '                                       />\n' +
  '                                </td>\n' +
  '                              </tr>\n' +
  '                            </tbody>\n' +
  '                          </table>\n' +
  '                        </td>\n' +
  '                      </tr>\n' +
  '                    </tbody>\n' +
  '                  </table>\n' +
  '                </div>\n' +
  '              </td>\n' +
  '            </tr>\n' +
  '          </tbody>\n' +
  '        </table>\n' +
  '      </div>\n' +
  '      <div  style="margin:0px auto;max-width:600px;">\n' +
  '        <table\n' +
  '               align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="width:100%;"\n' +
  '               >\n' +
  '          <tbody>\n' +
  '            <tr>\n' +
  '              <td\n' +
  '                  style="direction:ltr;font-size:0px;padding:20px 0;text-align:center;"\n' +
  '                  >\n' +
  '                <div\n' +
  '                     class="mj-column-per-100 mj-outlook-group-fix" style="font-size:0px;text-align:left;direction:ltr;display:inline-block;vertical-align:top;width:100%;"\n' +
  '                     >\n' +
  '                  <table\n' +
  '                         border="0" cellpadding="0" cellspacing="0" role="presentation" style="vertical-align:top;" width="100%"\n' +
  '                         >\n' +
  '                    <tbody>\n' +
  '                      <tr>\n' +
  '                        <td\n' +
  '                            align="left" style="font-size:0px;padding:10px 25px;word-break:break-word;"\n' +
  '                            >\n' +
  '                          <div\n' +
  '                               style="font-family:Ubuntu, Helvetica, Arial, sans-serif;font-size:15px;font-weight:700;line-height:1;text-align:left;color:#000000;"\n' +
  '                               >Section Heading\n' +
  '                          </div>\n' +
  '                        </td>\n' +
  '                      </tr>\n' +
  '                      <tr>\n' +
  '                        <td\n' +
  '                            align="left" style="font-size:0px;padding:10px 25px;word-break:break-word;"\n' +
  '                            >\n' +
  '                          <div\n' +
  '                               style="font-family:Ubuntu, Helvetica, Arial, sans-serif;font-size:13px;line-height:1;text-align:left;color:#000000;"\n' +
  '                               >\n' +
  '                            <span id="ie176">Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</span>\n' +
  '                          </div>\n' +
  '                        </td>\n' +
  '                      </tr>\n' +
  '                      <tr>\n' +
  '                        <td\n' +
  '                            align="center" vertical-align="middle" style="font-size:0px;padding:10px 25px;word-break:break-word;"\n' +
  '                            >\n' +
  '                          <table\n' +
  '                                 border="0" cellpadding="0" cellspacing="0" role="presentation" style="border-collapse:separate;line-height:100%;"\n' +
  '                                 >\n' +
  '                            <tbody>\n' +
  '                              <tr>\n' +
  '                                <td\n' +
  '                                    align="center" bgcolor="#414141" role="presentation" style="border:none;border-radius:3px;cursor:auto;mso-padding-alt:10px 25px;background:#414141;" valign="middle"\n' +
  '                                    >\n' +
  '                                  <a\n' +
  '                                     href="https://www.mybillone.com" style="display:inline-block;background:#414141;color:#ffffff;font-family:Ubuntu, Helvetica, Arial, sans-serif;font-size:13px;font-weight:normal;line-height:120%;margin:0;text-decoration:none;text-transform:none;padding:10px 25px;mso-padding-alt:0px;border-radius:3px;" target="_blank"\n' +
  '                                     >\n' +
  '                                    Button\n' +
  '                                  </a>\n' +
  '                                </td>\n' +
  '                              </tr>\n' +
  '                            </tbody>\n' +
  '                          </table>\n' +
  '                        </td>\n' +
  '                      </tr>\n' +
  '                    </tbody>\n' +
  '                  </table>\n' +
  '                </div>\n' +
  '              </td>\n' +
  '            </tr>\n' +
  '          </tbody>\n' +
  '        </table>\n' +
  '      </div>\n' +
  '      <div  style="margin:0px auto;max-width:600px;">\n' +
  '        <table\n' +
  '               align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="width:100%;"\n' +
  '               >\n' +
  '          <tbody>\n' +
  '            <tr>\n' +
  '              <td\n' +
  '                  style="direction:ltr;font-size:0px;padding:20px 0;text-align:center;"\n' +
  '                  >\n' +
  '                <div\n' +
  '                     class="mj-column-per-100 mj-outlook-group-fix" style="font-size:0px;text-align:left;direction:ltr;display:inline-block;vertical-align:top;width:100%;"\n' +
  '                     >\n' +
  '                  <table\n' +
  '                         border="0" cellpadding="0" cellspacing="0" role="presentation" style="vertical-align:top;" width="100%"\n' +
  '                         >\n' +
  '                    <tbody>\n' +
  '                      <tr>\n' +
  '                        <td\n' +
  '                            align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;"\n' +
  '                            >\n' +
  '                          <div\n' +
  '                               style="font-family:Ubuntu, Helvetica, Arial, sans-serif;font-size:13px;line-height:1;text-align:center;color:#000000;"\n' +
  '                               >\n' +
  '                            <a href="{{unsubscribe_link}}" title="Unsubscribe Link" target="_blank">Unsubscribe</a>\n' +
  '                          </div>\n' +
  '                        </td>\n' +
  '                      </tr>\n' +
  '                    </tbody>\n' +
  '                  </table>\n' +
  '                </div>\n' +
  '              </td>\n' +
  '            </tr>\n' +
  '          </tbody>\n' +
  '        </table>\n' +
  '      </div>\n' +
  '    </div>\n' +
  '  </body>\n' +
  '</html>\n'
