"""empty message

Revision ID: 8696c212b4b8
Revises: 1145ed83b305
Create Date: 2020-06-05 14:56:55.363512

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '8696c212b4b8'
down_revision = '1145ed83b305'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_column('examinee', 'name')
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.add_column('examinee', sa.Column('name', sa.VARCHAR(length=64), nullable=True))
    # ### end Alembic commands ###
