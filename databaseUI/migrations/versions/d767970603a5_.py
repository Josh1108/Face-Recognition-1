"""empty message

Revision ID: d767970603a5
Revises: f4fda1a54f82
Create Date: 2020-06-04 16:13:25.010377

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = 'd767970603a5'
down_revision = 'f4fda1a54f82'
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.add_column('examinee', sa.Column('name', sa.String(length=64), nullable=False))
    op.create_unique_constraint(None, 'examinee', ['name'])
    op.drop_column('examinee', 'image')
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.add_column('examinee', sa.Column('image', sa.VARCHAR(length=64), nullable=True))
    op.drop_constraint(None, 'examinee', type_='unique')
    op.drop_column('examinee', 'name')
    # ### end Alembic commands ###