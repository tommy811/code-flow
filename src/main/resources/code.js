
/**
 * java 代码流程转换
 * code flow plugin.
 */

Draw.loadPlugin(function (ui) {
    // 声明Action
    mxResources.parse('codeAction=Hello, World!');
    // 添加Action
    ui.actions.addAction('codeAction', function () {
        var codeDialog = new TextareaDialog(ui,
           '输入JAVA代码片段：',
            '', mxUtils.bind(ui, function (newValue) {

                let data=new FormData();
                data.append("code",newValue);
                fetch('http://127.0.0.1:9080/flow/java',
                    {
                        method: "POST",
                        mode: 'cors',
                        body:data
                    }).then(function (resp) {
                    return resp.text();
                }).then(function (csvData) {
                    console.log(csvData);
                    ui.importCsv(csvData);
                });
            }), null, null, 620, 430, null, true, true, mxResources.get('import'),
            'https://shimo.im/docs/66y9WjdgqPr9rdqY/');
        ui.showDialog(codeDialog.container, 640, 520, true, true, null, null, null, null, true);
        codeDialog.init();
    });
    // 添加分割栏
    ui.toolbar.addSeparator();
    var elt = ui.toolbar.addItem('', 'codeAction');
    // 设置样式
    elt.firstChild.style.backgroundImage = 'url(data:image/svg+xml;base64,PHN2ZyB0PSIxNjE5MTQwOTk0ODU4IiBjbGFzcz0iaWNvbiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjIyMTUiCiAgICAgd2lkdGg9IjE2IiBoZWlnaHQ9IjE2Ij4KICAgIDxwYXRoIGQ9Ik00ODAuMzk1MDYyIDEwMTEuMzU4MDI1di0yMjEuMjM0NTY4YzAtNTMuNzI4Mzk1LTQxLjA4NjQyLTk0LjgxNDgxNS05NC44MTQ4MTUtOTQuODE0ODE1cy05NC44MTQ4MTUgNDEuMDg2NDItOTQuODE0ODE1IDk0LjgxNDgxNXYyMjEuMjM0NTY4aDYzLjIwOTg3N3YtMTI2LjQxOTc1M2g2My4yMDk4NzZ2MTI2LjQxOTc1M2g2My4yMDk4Nzd6IG0tMTI2LjQxOTc1My0xODkuNjI5NjN2LTMxLjYwNDkzOGMwLTE4Ljk2Mjk2MyAxMi42NDE5NzUtMzEuNjA0OTM4IDMxLjYwNDkzOC0zMS42MDQ5MzhzMzEuNjA0OTM4IDEyLjY0MTk3NSAzMS42MDQ5MzggMzEuNjA0OTM4djMxLjYwNDkzOGgtNjMuMjA5ODc2ek0zNy45MjU5MjYgODUzLjMzMzMzM3Y2My4yMDk4NzdjMCA1MC41Njc5MDEgNDEuMDg2NDIgOTQuODE0ODE1IDk0LjgxNDgxNSA5NC44MTQ4MTVzOTQuODE0ODE1LTQxLjA4NjQyIDk0LjgxNDgxNS05NC44MTQ4MTV2LTE1OC4wMjQ2OTFoMzEuNjA0OTM4di02My4yMDk4NzdIMTMyLjc0MDc0MXY2My4yMDk4NzdoMzEuNjA0OTM4djE1OC4wMjQ2OTFjMCAxNS44MDI0NjktMTIuNjQxOTc1IDMxLjYwNDkzOC0zMS42MDQ5MzggMzEuNjA0OTM4cy0zMS42MDQ5MzgtMTIuNjQxOTc1LTMxLjYwNDkzOS0zMS42MDQ5Mzh2LTYzLjIwOTg3N0gzNy45MjU5MjZ6TTU0My42MDQ5MzggNjk1LjMwODY0MnYyMzMuODc2NTQzbDk0LjgxNDgxNSA5NC44MTQ4MTUgOTQuODE0ODE1LTk0LjgxNDgxNVY2OTUuMzA4NjQyaC02My4yMDk4Nzd2MjA4LjU5MjU5M2wtMzEuNjA0OTM4IDMxLjYwNDkzOC0zMS42MDQ5MzgtMzEuNjA0OTM4VjY5NS4zMDg2NDJ6TTk4Ni4wNzQwNzQgMTAxMS4zNTgwMjV2LTIyMS4yMzQ1NjhjMC01My43MjgzOTUtNDEuMDg2NDItOTQuODE0ODE1LTk0LjgxNDgxNS05NC44MTQ4MTVzLTk0LjgxNDgxNSA0MS4wODY0Mi05NC44MTQ4MTUgOTQuODE0ODE1djIyMS4yMzQ1NjhoNjMuMjA5ODc3di0xMjYuNDE5NzUzaDYzLjIwOTg3N3YxMjYuNDE5NzUzaDYzLjIwOTg3NnogbS0xMjYuNDE5NzUzLTE4OS42Mjk2M3YtMzEuNjA0OTM4YzAtMTguOTYyOTYzIDEyLjY0MTk3NS0zMS42MDQ5MzggMzEuNjA0OTM4LTMxLjYwNDkzOHMzMS42MDQ5MzggMTIuNjQxOTc1IDMxLjYwNDkzOSAzMS42MDQ5Mzh2MzEuNjA0OTM4aC02My4yMDk4Nzd6TTYuMzIwOTg4IDYzMi4wOTg3NjVoNjMuMjA5ODc2VjI1Mi44Mzk1MDZoODg0LjkzODI3MnYzNzkuMjU5MjU5aDYzLjIwOTg3NlYwSDYuMzIwOTg4djYzMi4wOTg3NjV6TTY5LjUzMDg2NCA2My4yMDk4NzdoODg0LjkzODI3MnYxMjYuNDE5NzUzSDY5LjUzMDg2NFY2My4yMDk4Nzd6IgogICAgICAgICAgcC1pZD0iMjIxNiI+PC9wYXRoPgogICAgPHBhdGggZD0iTTEzMi43NDA3NDEgOTQuODE0ODE1aDYzLjIwOTg3NnY2My4yMDk4NzZIMTMyLjc0MDc0MXpNMjU5LjE2MDQ5NCA5NC44MTQ4MTVoNjMuMjA5ODc2djYzLjIwOTg3NkgyNTkuMTYwNDk0ek0zODUuNTgwMjQ3IDk0LjgxNDgxNWg2My4yMDk4NzZ2NjMuMjA5ODc2aC02My4yMDk4NzZ6IgogICAgICAgICAgcC1pZD0iMjIxNyI+PC9wYXRoPgo8L3N2Zz4=)';
    elt.firstChild.style.backgroundPosition = '2px 3px';
});