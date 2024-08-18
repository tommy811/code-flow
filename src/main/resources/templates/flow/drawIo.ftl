# label: &nbsp;&nbsp;%name%&nbsp;&nbsp;
## style: shape=%shape%;fillColor=#ffffff;strokeColor=#000000;html=1;
# styles: ${styles_json}
# stylename: styletype
# namespace: csvimport-
# connect: {"from": "r_yes", "to": "id", "label": "yes", "style": "${styles.r_yes}"}
# connect: {"from": "r_no", "to": "id",  "label": "no", "style": "${styles.r_no}"}
# connect: {"from": "r_next", "to": "id",   "style": "${styles.r_normal}"}
# connect: {"from": "r_union", "to": "id",   "style": "${styles.r_union}"}
# connect: {"from": "r_tips", "to": "id",   "style": "${styles.r_tips}"}
# width: auto
# height: auto
# padding: 5
# ignore: next,yes,no
# nodespacing: 60
# levelspacing: 120
# edgespacing: 40
# layout: ${layout}
## **********************************************************
## CSV DATA
## **********************************************************
id,name,shape,type,styletype,${joinCloumns}
${dataRows}